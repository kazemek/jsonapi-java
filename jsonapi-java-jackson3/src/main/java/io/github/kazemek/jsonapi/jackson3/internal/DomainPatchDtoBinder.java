package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import io.github.kazemek.jsonapi.jackson3.RelationshipLinkageMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Binds a validated single-resource update directly into an application-owned annotated PATCH DTO.
 *
 * <p>Every resolver-classified attribute and relationship member of the PATCH DTO must be declared
 * exactly as {@code PatchPresence<T>} without wrapper-level {@code @JsonDeserialize} /
 * {@code @JsonSerialize}; violations fail with {@link
 * MappingDiagnostic#INVALID_PATCH_PROPERTY_TYPE} at the property path before any member binds. The
 * identifier property is a normal identifier (never a patchable member). Supplied members convert
 * against the unwrapped inner {@code T} type (converting explicit JSON {@code null} through it
 * first) and are placed into a synthetic property map as an internal {@link PresenceMarker}, then
 * the bean is constructed with a single {@link JsonMapper#convertValue(Object, JavaType)} so
 * creators, deserializers, converters, and configured modules remain authoritative (ADR-004).
 * Omitted members bind to {@code PatchPresence.omitted()}; supplied unknown members fail with
 * {@link MappingDiagnostic#UNKNOWN_PATCH_MEMBER}. Document {@code included} is never read.
 *
 * <p>Recursive structured attributes (ADR-014) use the {@link StructuredValueBinder}: a nested
 * member whose inner type is a deliberately presence-aware PATCH shape is bound as a complete
 * nested {@link PresenceMarker} tree so the single whole-tree {@code convertValue} preserves the
 * strict marker invariant. Deep Jackson construction-failure paths are translated to wire-name
 * pointers through the resolved shape metadata.
 */
public final class DomainPatchDtoBinder {

  private static final String IDENTIFIER_PATH_ID = "/id";
  private static final String ATTRIBUTE_PATH_PREFIX = "/attributes";

  private final JsonMapper mapper;
  private final MappingDefinitionCache cache;
  private final PatchMemberConverter converter;
  private final StructuredValueBinder structuredBinder;

  public DomainPatchDtoBinder(
      JsonMapper mapper,
      IdentifierConverter identifierConverter,
      MappingDefinitionCache cache,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.cache = Objects.requireNonNull(cache, "cache");
    this.converter = new PatchMemberConverter(mapper, identifierConverter, linkageMappers);
    this.structuredBinder = new StructuredValueBinder(mapper);
  }

  /** Binds one resource object into a PATCH DTO instance of {@code targetType}. */
  public Object fromResource(ResourceObject resource, JavaType targetType) {
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(targetType, "targetType");
    Class<?> rawType = targetType.getRawClass();
    ResourceMapping mapping = cache.resolve(targetType);
    validateResourceType(resource, mapping, rawType);
    validatePatchDtoDeclaration(mapping, rawType);
    Map<String, @Nullable Object> properties = new LinkedHashMap<>();
    bindIdentity(resource, mapping, properties, rawType);
    bindAttributes(resource, mapping, properties, rawType);
    bindRelationships(resource, mapping, properties, rawType);
    Map<String, MappingProperty> attributesByLogicalName =
        PatchMemberConverter.byLogicalName(mapping.attributes());
    return BeanConstruction.convertBean(
        mapper,
        properties,
        targetType,
        rawType,
        (failure, ignored) -> translateConstructionPath(failure, attributesByLogicalName));
  }

  private void validateResourceType(
      ResourceObject resource, ResourceMapping mapping, Class<?> rawType) {
    String expectedType = mapping.resourceType();
    if (!expectedType.equals(resource.type())) {
      throw new JsonApiMappingException(
          MappingDiagnostic.RESOURCE_TYPE_MISMATCH,
          rawType,
          "/type",
          "Resource object type '"
              + resource.type()
              + "' does not match expected type '"
              + expectedType
              + "'");
    }
  }

  /**
   * PATCH DTO declaration check: every attribute and relationship member (including implicit-role
   * unannotated members that default to the attribute role) must be exactly {@code
   * PatchPresence<T>} and must not carry wrapper-level Jackson customization.
   */
  private void validatePatchDtoDeclaration(ResourceMapping mapping, Class<?> rawType) {
    MappingProperty identifier = mapping.identifierProperty();
    if (identifier != null && isPatchPresenceType(identifier.accessor().getType())) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE,
          rawType,
          "/" + identifier.logicalName(),
          "PATCH DTO identifier '"
              + identifier.logicalName()
              + "' must not be declared as PatchPresence on "
              + rawType.getName());
    }
    for (MappingProperty property : mapping.attributes()) {
      validatePatchableProperty(property, rawType);
    }
    for (MappingProperty property : mapping.relationships()) {
      validatePatchableProperty(property, rawType);
    }
  }

  private void validatePatchableProperty(MappingProperty property, Class<?> rawType) {
    JavaType type = property.definition().getPrimaryType();
    if (!isPatchPresenceType(type)
        || WrapperCustomization.has(
            mapper, type, property.accessor(), property.definition().getMutator())) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE,
          rawType,
          "/" + property.logicalName(),
          "PATCH DTO member '"
              + property.logicalName()
              + "' must be declared exactly as PatchPresence<T> without wrapper-level "
              + "@JsonDeserialize/@JsonSerialize customization on "
              + rawType.getName());
    }
  }

  private static boolean isPatchPresenceType(JavaType type) {
    return type.getRawClass() == PatchPresence.class && type.containedTypeCount() == 1;
  }

  private void bindIdentity(
      ResourceObject resource,
      ResourceMapping mapping,
      Map<String, @Nullable Object> properties,
      Class<?> rawType) {
    MappingProperty identifierProperty = mapping.identifierProperty();
    if (identifierProperty == null || !resource.hasId()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED,
          rawType,
          IDENTIFIER_PATH_ID,
          "Resource update identity requires a non-null id at '" + IDENTIFIER_PATH_ID + "'");
    }
    Object identity =
        converter.convertIdentity(
            Objects.requireNonNull(resource.id()), identifierProperty, rawType);
    properties.put(identifierProperty.logicalName(), identity);
  }

  private void bindAttributes(
      ResourceObject resource,
      ResourceMapping mapping,
      Map<String, @Nullable Object> properties,
      Class<?> rawType) {
    Attributes attributes = resource.attributes();
    Map<String, @Nullable Object> supplied = attributes == null ? null : attributes.attributes();
    Map<String, MappingProperty> byJsonapiName =
        PatchMemberConverter.byJsonapiName(mapping.attributes());
    if (supplied != null) {
      for (String name : supplied.keySet()) {
        if (!byJsonapiName.containsKey(name)) {
          throw unknownPatchMember(rawType, ATTRIBUTE_PATH_PREFIX + "/" + name, "attribute", name);
        }
      }
    }
    for (MappingProperty property : mapping.attributes()) {
      if (supplied != null && supplied.containsKey(property.jsonapiName())) {
        Object value =
            structuredBinder.typedMemberValue(
                supplied.get(property.jsonapiName()),
                property.accessor().getType(),
                ATTRIBUTE_PATH_PREFIX + "/" + property.jsonapiName(),
                rawType);
        properties.put(property.logicalName(), new PresenceMarker(true, value));
      } else {
        properties.put(property.logicalName(), new PresenceMarker(false, null));
      }
    }
  }

  private void bindRelationships(
      ResourceObject resource,
      ResourceMapping mapping,
      Map<String, @Nullable Object> properties,
      Class<?> rawType) {
    Relationships relationships = resource.relationships();
    Map<String, Relationship> supplied =
        relationships == null ? null : relationships.relationships();
    Map<String, MappingProperty> byJsonapiName =
        PatchMemberConverter.byJsonapiName(mapping.relationships());
    if (supplied != null) {
      for (String name : supplied.keySet()) {
        if (!byJsonapiName.containsKey(name)) {
          throw unknownPatchMember(rawType, "/relationships/" + name, "relationship", name);
        }
      }
    }
    for (MappingProperty property : mapping.relationships()) {
      Relationship relationship = supplied == null ? null : supplied.get(property.jsonapiName());
      RelationshipData data = relationship == null ? null : relationship.data();
      if (data == null) {
        // Omitted, or a supplied mapped relationship lacking data (only reachable on the
        // non-revalidating fromDocument path): bind as Omitted, mirroring the low-level skip.
        properties.put(property.logicalName(), new PresenceMarker(false, null));
        continue;
      }
      Object value = converter.convertRelationship(property, data, innerType(property));
      properties.put(property.logicalName(), new PresenceMarker(true, value));
    }
  }

  private static JsonApiMappingException unknownPatchMember(
      Class<?> rawType, String path, String kind, String name) {
    return new JsonApiMappingException(
        MappingDiagnostic.UNKNOWN_PATCH_MEMBER,
        rawType,
        path,
        "Unknown supplied " + kind + " '" + name + "' for PATCH DTO " + rawType.getName());
  }

  private static JavaType innerType(MappingProperty property) {
    return property.accessor().getType().containedType(0);
  }

  /**
   * Translates a failed single-pass bean-construction Jackson path into a resource-relative
   * wire-name pointer for structured attributes (ADR-014).
   *
   * <p>The path's first name is the top-level synthetic-map key (the attribute's Jackson logical
   * name); it is mapped to the JSON:API wire name via the resolved mapping. Deeper names are walked
   * through the already-resolved presence-aware shape metadata: each name that matches a nested
   * member contributes that member's wire name, and the internal marker {@code value} member
   * between two shape levels is skipped. Walking stops at the first name that is not a shape
   * member, so Jackson-internal names below an atomic member are never leaked into the pointer.
   */
  private String translateConstructionPath(
      Throwable failure, Map<String, MappingProperty> attributesByLogicalName) {
    List<String> names = BeanConstruction.pathNames(failure);
    if (names.isEmpty()) {
      return "/";
    }
    MappingProperty top = attributesByLogicalName.get(names.getFirst());
    if (top == null) {
      return BeanConstruction.propertyPath(failure);
    }
    StringBuilder pointer =
        new StringBuilder(ATTRIBUTE_PATH_PREFIX).append('/').append(top.jsonapiName());
    JavaType current = top.accessor().getType();
    int i = 1;
    boolean walking = true;
    while (i < names.size() && walking) {
      String name = names.get(i);
      StructuredValueBinder.Shape shape = structuredBinder.shapeOfStructured(current);
      StructuredValueBinder.Member member = shape == null ? null : shape.memberByWire(name);
      if (member == null) {
        if (shape != null && "value".equals(name)) {
          i++;
          continue;
        }
        walking = false;
      } else {
        pointer.append('/').append(PointerEscapes.escape(member.wireName()));
        current = member.type();
        i++;
      }
    }
    return pointer.toString();
  }
}
