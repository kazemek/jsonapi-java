package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException;
import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext;
import io.github.kazemek.jsonapi.jackson.FieldPolicy;
import io.github.kazemek.jsonapi.jackson.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.MappingLocation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

public final class DomainResourceWriter {

  private static final String RESOURCE = "resource";

  private final IdentifierConverter identifierConverter;
  private final MappingDefinitionCache cache;
  private final WholeMetaTarget wholeMetaTarget;
  private final PropertyScopedValueConverter propertyScoped;

  public DomainResourceWriter(
      JsonMapper mapper, IdentifierConverter identifierConverter, MappingDefinitionCache cache) {
    this.identifierConverter = Objects.requireNonNull(identifierConverter, "identifierConverter");
    this.cache = Objects.requireNonNull(cache, "cache");
    this.wholeMetaTarget = new WholeMetaTarget(mapper);
    this.propertyScoped = new PropertyScopedValueConverter(mapper);
  }

  /**
   * Selective emission result: the mapped resource plus whether any relationship member was omitted
   * because a present fieldset for that resource's type did not include that relationship name.
   */
  public record SelectiveResource(ResourceObject resource, boolean relationshipOmittedByFieldset) {

    public SelectiveResource {
      Objects.requireNonNull(resource, RESOURCE);
    }
  }

  public ResourceObject toResource(Object resource) {
    Objects.requireNonNull(resource, RESOURCE);
    ResourceMapping mapping = cache.resolve(resource.getClass());
    validateMetaTargets(mapping, resource.getClass());
    String id = extractId(resource, mapping);
    Attributes attributes = buildAttributes(resource, mapping, null);
    Relationships relationships = buildRelationships(resource, mapping, null);
    Meta meta = buildResourceMeta(resource, mapping);
    return buildResourceObject(mapping, id, attributes, relationships, meta);
  }

  /**
   * Selective emission using fieldsets and {@link FieldPolicy} from {@code context}. Validates a
   * present fieldset entry for the resource's mapped type before any selective attribute or
   * relationship reads.
   */
  public SelectiveResource toResource(Object resource, CompoundSerializationContext context) {
    Objects.requireNonNull(resource, RESOURCE);
    Objects.requireNonNull(context, "context");
    ResourceMapping mapping = cache.resolve(resource.getClass());
    List<String> fields = fieldsFor(context, mapping.resourceType());
    if (fields != null) {
      validateFieldset(resource.getClass(), mapping, fields, context.fieldPolicy());
    }
    return toResource(resource, fields);
  }

  /**
   * Selective emission. {@code fields == null} means unrestricted; empty selects no
   * attributes/relationships (non-field resource members such as mapped resource meta remain
   * independent); non-empty is an allow-list of JSON:API attribute and relationship names. Does not
   * consult {@link FieldPolicy}; callers that need policy checks must validate first (or use {@link
   * #toResource(Object, CompoundSerializationContext)}).
   */
  public SelectiveResource toResource(Object resource, @Nullable List<String> fields) {
    Objects.requireNonNull(resource, RESOURCE);
    if (fields == null) {
      return new SelectiveResource(toResource(resource), false);
    }
    ResourceMapping mapping = cache.resolve(resource.getClass());
    validateMetaTargets(mapping, resource.getClass());
    String id = extractId(resource, mapping);
    Set<String> allowed = Set.copyOf(fields);
    boolean relationshipOmitted = false;
    for (MappingProperty property : mapping.relationships()) {
      if (!allowed.contains(property.jsonapiName())) {
        relationshipOmitted = true;
        break;
      }
    }
    Attributes attributes = buildAttributes(resource, mapping, allowed);
    Relationships relationships = buildRelationships(resource, mapping, allowed);
    Meta meta = buildResourceMeta(resource, mapping);
    return new SelectiveResource(
        buildResourceObject(mapping, id, attributes, relationships, meta), relationshipOmitted);
  }

  /**
   * Resolves the fieldset list for {@code resourceType}: {@code null} when the type key is absent
   * (unrestricted), otherwise the stored list (possibly empty, selecting no
   * attributes/relationships).
   */
  public static @Nullable List<String> fieldsFor(
      CompoundSerializationContext context, String resourceType) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(resourceType, "resourceType");
    Map<String, List<String>> fieldsets = context.fieldsets();
    if (!fieldsets.containsKey(resourceType)) {
      return null;
    }
    return fieldsets.get(resourceType);
  }

  private static void validateFieldset(
      Class<?> resourceClass,
      ResourceMapping mapping,
      List<String> fields,
      FieldPolicy fieldPolicy) {
    if (fields.isEmpty()) {
      return;
    }
    Set<String> mappedNames = new HashSet<>();
    for (MappingProperty property : mapping.attributes()) {
      mappedNames.add(property.jsonapiName());
    }
    for (MappingProperty property : mapping.relationships()) {
      mappedNames.add(property.jsonapiName());
    }
    for (String name : fields) {
      if (!mappedNames.contains(name)) {
        // Fieldset specification failures have no document member location; the offending field
        // name stays in the message per the mapping-location contract.
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.INVALID_FIELDSET_FIELD,
            resourceClass,
            "Unknown fieldset field '" + name + "' on " + mapping.resourceType());
      }
      if (!fieldPolicy.allows(mapping.resourceType(), name)) {
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.DENIED_FIELDSET_FIELD,
            resourceClass,
            "Fieldset field denied for " + mapping.resourceType() + "." + name);
      }
    }
  }

  private static ResourceObject buildResourceObject(
      ResourceMapping mapping,
      @Nullable String id,
      Attributes attributes,
      Relationships relationships,
      @Nullable Meta meta) {
    return new ResourceObject(
        mapping.resourceType(),
        id,
        null,
        attributes.isEmpty() ? null : attributes,
        relationships.isEmpty() ? null : relationships,
        null,
        meta,
        Map.of());
  }

  /**
   * Whole-meta declared-target validation for the read/write domain-mapping role: Bean / Map /
   * Object with at most one {@link Optional} wrapper. Validation lives at the consuming entry
   * point, not the kind-agnostic resolver (ADR-015).
   */
  private void validateMetaTargets(ResourceMapping mapping, Class<?> rawType) {
    wholeMetaTarget.validateReadWriteTargets(mapping, rawType);
  }

  @Nullable String extractId(Object resource, ResourceMapping mapping) {
    MappingProperty identifierProperty = mapping.identifierProperty();
    if (identifierProperty == null) {
      return null;
    }
    Object identifierValue =
        unwrapOptional(readValue(resource, identifierProperty, PropertyRole.ID));
    return requireIdentifierString(resource.getClass(), identifierProperty, identifierValue);
  }

  private String requireIdentifierString(
      Class<?> type, MappingProperty property, @Nullable Object identifierValue) {
    if (identifierValue == null) {
      throw missingIdentifier(type, "Identifier property '" + property.logicalName() + "' is null");
    }
    String identifierString = identifierConverter.convert(identifierValue);
    if (identifierString == null) {
      throw missingIdentifier(
          type, "Identifier converter returned null for property '" + property.logicalName() + "'");
    }
    return identifierString;
  }

  public ResourceIdentifier extractIdentifier(Object resource) {
    Objects.requireNonNull(resource, RESOURCE);
    Class<?> rawType = resource.getClass();
    ResourceMapping mapping = cache.resolve(rawType);
    String id = extractId(resource, mapping);
    return new ResourceIdentifier(mapping.resourceType(), id, null, null, Map.of());
  }

  /** Resolves the cached mapping definition for {@code rawType}. */
  ResourceMapping mappingFor(Class<?> rawType) {
    return cache.resolve(rawType);
  }

  /** Reads a relationship property for inclusion traversal (not linkage construction). */
  @Nullable Object readRelationshipValue(Object resource, MappingProperty property) {
    return readValue(resource, property, PropertyRole.RELATIONSHIP);
  }

  static boolean isToManyType(JavaType type) {
    if (type.isArrayType()) {
      return true;
    }
    if (type.isCollectionLikeType()) {
      return true;
    }
    return type.isTypeOrSubTypeOf(Iterable.class);
  }

  static @Nullable JavaType resolveContentType(JavaType type) {
    if (type.isArrayType() || type.isCollectionLikeType()) {
      return type.getContentType();
    }
    if (type.containedTypeCount() > 0) {
      return type.containedType(0);
    }
    return null;
  }

  static @Nullable Object unwrapOptional(@Nullable Object value) {
    if (value instanceof Optional<?> optional) {
      return optional.orElse(null);
    }
    return value;
  }

  /**
   * Converts an already-read to-many value into a list of elements. Serialization callers supply
   * the failing relationship's resource-relative location; inclusion traversal, which has no
   * JSON:API member coordinate for this value, uses {@link #convertToCollection(Object)}.
   */
  static List<Object> convertToCollection(
      Object value, @Nullable MappingLocation relationshipLocation) {
    return switch (value) {
      case List<?> list -> {
        List<Object> result = new ArrayList<>(list.size());
        result.addAll(list);
        yield result;
      }
      case Object[] array -> {
        List<Object> result = new ArrayList<>(array.length);
        Collections.addAll(result, array);
        yield result;
      }
      case Iterable<?> iterable -> {
        List<Object> result = new ArrayList<>();
        for (Object item : iterable) {
          result.add(item);
        }
        yield result;
      }
      default -> throw relationshipShapeFailure(value, relationshipLocation);
    };
  }

  /**
   * Locationless variant for callers without a JSON:API member coordinate (inclusion traversal).
   */
  static List<Object> convertToCollection(Object value) {
    return convertToCollection(value, null);
  }

  private static JsonApiMappingException relationshipShapeFailure(
      Object value, @Nullable MappingLocation relationshipLocation) {
    String message =
        "To-many relationship value is not a supported collection type: "
            + value.getClass().getName();
    return relationshipLocation == null
        ? JsonApiMappingException.withoutLocation(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE, value.getClass(), message)
        : new JsonApiMappingException(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
            value.getClass(),
            relationshipLocation,
            message);
  }

  private Attributes buildAttributes(
      Object resource, ResourceMapping mapping, @Nullable Set<String> allowedFields) {
    if (mapping.attributes().isEmpty()) {
      return Attributes.empty();
    }
    Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
    for (MappingProperty property : mapping.attributes()) {
      if (allowedFields == null || allowedFields.contains(property.jsonapiName())) {
        Object rawValue = readValue(resource, property, PropertyRole.ATTRIBUTE);
        if (!(rawValue instanceof Optional<?> optional) || optional.isPresent()) {
          PropertyScopedValueConverter.SerializationResult converted =
              convertAttributeValue(resource, mapping, property, rawValue);
          if (converted.emitted()) {
            attributes.put(property.jsonapiName(), converted.value());
          }
        }
      }
    }
    return Attributes.ofAttributes(attributes);
  }

  private Relationships buildRelationships(
      Object resource, ResourceMapping mapping, @Nullable Set<String> allowedFields) {
    if (mapping.relationships().isEmpty()) {
      return Relationships.empty();
    }
    Map<String, MappingProperty> relationshipMetaByTarget =
        RelationshipMetaSupport.byTarget(mapping.relationshipMetaProperties());
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    for (MappingProperty property : mapping.relationships()) {
      if (allowedFields != null && !allowedFields.contains(property.jsonapiName())) {
        continue;
      }
      relationships.put(
          property.jsonapiName(),
          buildRelationship(resource, mapping, property, relationshipMetaByTarget));
    }
    return Relationships.ofRelationships(relationships);
  }

  private Relationship buildRelationship(
      Object resource,
      ResourceMapping mapping,
      MappingProperty property,
      Map<String, MappingProperty> relationshipMetaByTarget) {
    Object value = readValue(resource, property, PropertyRole.RELATIONSHIP);
    JavaType propertyType = property.accessor().getType();
    MappingLocation relationshipLocation =
        RelationshipLinkageSupport.relationshipLocation(property);
    RelationshipData linkage =
        isToManyType(propertyType)
            ? extractToManyLinkage(value, propertyType, relationshipLocation)
            : extractToOneLinkage(value);
    Meta meta = null;
    MappingProperty metaProperty = relationshipMetaByTarget.get(property.jsonapiName());
    if (metaProperty != null) {
      meta =
          buildMetaValue(
              resource,
              mapping,
              metaProperty,
              RelationshipMetaSupport.relationshipMetaLocation(property.jsonapiName()));
    }
    return new Relationship(linkage, null, meta, Map.of());
  }

  /** Builds the resource-side {@code meta} from the single mapped resource-meta property. */
  private @Nullable Meta buildResourceMeta(Object resource, ResourceMapping mapping) {
    MappingProperty resourceMetaProperty = mapping.resourceMeta();
    if (resourceMetaProperty == null) {
      return null;
    }
    return buildMetaValue(
        resource, mapping, resourceMetaProperty, RelationshipMetaSupport.resourceMetaLocation());
  }

  /**
   * Converts one whole-meta property value into a core {@link Meta}. The converted result must be a
   * {@link Map}; scalar/array/non-object runtime values fail with a stable meta diagnostic (never a
   * leaked cast or core-validation failure). Failures report the location-specific {@code path}.
   */
  private @Nullable Meta buildMetaValue(
      Object resource,
      ResourceMapping mapping,
      MappingProperty property,
      MappingLocation metaLocation) {
    Object rawValue = readValue(resource, property, property.role());
    Object value = unwrapOptional(rawValue);
    if (value == null) {
      return null;
    }
    Object converted;
    try {
      PropertyScopedValueConverter.SerializationResult serialized =
          propertyScoped.serialize(
              mapping.domainType(),
              property.definition().getFullName().getSimpleName(),
              resource,
              rawValue,
              value);
      if (!serialized.emitted()) {
        return null;
      }
      converted = serialized.value();
    } catch (RuntimeException e) {
      throw metaValueFailure(resource, metaLocation, "Failed to convert meta value", e);
    }
    if (!(converted instanceof Map<?, ?> map)) {
      throw metaValueFailure(
          resource,
          metaLocation,
          "Converted meta value is not an object (expected a JSON object, got "
              + convertedTypeName(converted)
              + ")",
          null);
    }
    try {
      return Meta.of(castMembers(map, resource, metaLocation));
    } catch (JsonApiValidationException e) {
      throw metaValueFailure(resource, metaLocation, "Invalid meta members", e);
    }
  }

  private static String convertedTypeName(@Nullable Object converted) {
    return converted == null ? "null" : converted.getClass().getName();
  }

  /**
   * Rebuilds the converted meta value into a string-keyed member map. Today the conversion target
   * {@code Object.class} always yields string keys (Jackson's untyped map representation), so the
   * non-string branch is defensive: it keeps the stable {@link
   * MappingDiagnostic#INVALID_META_TARGET} diagnostic at the known {@code metaLocation} instead of
   * leaking a class cast or a core-validation failure if a future Jackson version ever emits
   * non-string keys.
   */
  private static Map<String, Object> castMembers(
      Map<?, ?> map, Object resource, MappingLocation metaLocation) {
    Map<String, Object> members = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      Object key = entry.getKey();
      if (!(key instanceof String stringKey)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.INVALID_META_TARGET,
            resource.getClass(),
            metaLocation,
            "Meta object key is not a string: " + key);
      }
      members.put(stringKey, entry.getValue());
    }
    return members;
  }

  private JsonApiMappingException metaValueFailure(
      Object resource, MappingLocation metaLocation, String message, @Nullable Throwable cause) {
    return cause == null
        ? new JsonApiMappingException(
            MappingDiagnostic.INVALID_META_TARGET, resource.getClass(), metaLocation, message)
        : new JsonApiMappingException(
            MappingDiagnostic.INVALID_META_TARGET,
            resource.getClass(),
            metaLocation,
            message,
            cause);
  }

  private RelationshipData extractToOneLinkage(@Nullable Object value) {
    value = unwrapOptional(value);
    return switch (value) {
      case null -> RelationshipData.NullLinkage.INSTANCE;
      case ResourceIdentifier resourceIdentifier ->
          new RelationshipData.SingleLinkage(resourceIdentifier);
      case RelationshipData relationshipData -> relationshipData;
      default ->
          new RelationshipData.SingleLinkage(extractIdentifier(Objects.requireNonNull(value)));
    };
  }

  private RelationshipData extractToManyLinkage(
      @Nullable Object value, JavaType propType, MappingLocation relationshipLocation) {
    if (value == null) {
      return RelationshipData.IdentifierCollectionLinkage.empty();
    }
    List<Object> items = convertToCollection(value, relationshipLocation);
    if (items.isEmpty()) {
      return RelationshipData.IdentifierCollectionLinkage.empty();
    }
    return switch (classifyToManyItems(items)) {
      case ResourceIdentifiers(List<ResourceIdentifier> identifiers) ->
          new RelationshipData.IdentifierCollectionLinkage(identifiers);
      case DomainObjects(List<?> domainItems) ->
          toManyLinkageFromDomainObjects(domainItems, propType, relationshipLocation);
      case Mixed(Object firstNonResourceIdentifier) ->
          throw mixedToManyElements(firstNonResourceIdentifier, relationshipLocation);
    };
  }

  private static ToManyClassification classifyToManyItems(List<?> items) {
    boolean hasResourceIdentifier = false;
    Object firstNonResourceIdentifier = null;
    List<ResourceIdentifier> identifiers = new ArrayList<>();
    List<Object> domainItems = new ArrayList<>();
    for (Object item : items) {
      if (item == null) {
        continue;
      }
      if (item instanceof ResourceIdentifier resourceIdentifier) {
        hasResourceIdentifier = true;
        identifiers.add(resourceIdentifier);
      } else {
        if (firstNonResourceIdentifier == null) {
          firstNonResourceIdentifier = item;
        }
        domainItems.add(item);
      }
    }
    if (hasResourceIdentifier && firstNonResourceIdentifier != null) {
      return new Mixed(firstNonResourceIdentifier);
    }
    if (hasResourceIdentifier) {
      return new ResourceIdentifiers(identifiers);
    }
    return new DomainObjects(domainItems);
  }

  private RelationshipData toManyLinkageFromDomainObjects(
      List<?> items, JavaType propType, MappingLocation relationshipLocation) {
    JavaType contentType = resolveContentType(propType);
    if (contentType == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
          null,
          relationshipLocation,
          "Cannot resolve collection content type");
    }
    checkDeclaredTargetHasResourceMetadata(contentType.getRawClass(), relationshipLocation);
    List<ResourceIdentifier> identifiers = new ArrayList<>(items.size());
    for (Object item : items) {
      identifiers.add(extractIdentifier(item));
    }
    return new RelationshipData.IdentifierCollectionLinkage(identifiers);
  }

  private static JsonApiMappingException missingIdentifier(Class<?> type, String message) {
    return new JsonApiMappingException(
        MappingDiagnostic.MISSING_IDENTIFIER, type, MappingLocation.of("id"), message);
  }

  private static JsonApiMappingException mixedToManyElements(
      Object firstNonResourceIdentifier, MappingLocation relationshipLocation) {
    return new JsonApiMappingException(
        MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
        firstNonResourceIdentifier.getClass(),
        relationshipLocation,
        "Mixed element types in to-many relationship collection: expected ResourceIdentifier, got "
            + firstNonResourceIdentifier.getClass().getName());
  }

  private static @Nullable Object readValue(
      Object resource, MappingProperty property, PropertyRole role) {
    try {
      return property.accessor().getValue(resource);
    } catch (JsonApiMappingException e) {
      throw e;
    } catch (Exception e) {
      throw new JsonApiMappingException(
          diagnosticFor(role),
          resource.getClass(),
          memberLocation(property, role),
          "Failed to read property '"
              + property.logicalName()
              + "' ("
              + role.name().toLowerCase()
              + ")",
          e);
    }
  }

  /**
   * Resource-relative wire location of one mapped member, per the mapping-location contract: the
   * JSON:API member name is escaped as pointer segments, never the Jackson logical name.
   */
  private static MappingLocation memberLocation(MappingProperty property, PropertyRole role) {
    return switch (role) {
      case ID -> MappingLocation.of("id");
      case ATTRIBUTE -> MappingLocation.of("attributes", property.jsonapiName());
      case RELATIONSHIP -> MappingLocation.of("relationships", property.jsonapiName(), "data");
      case RESOURCE_META -> RelationshipMetaSupport.resourceMetaLocation();
      case RELATIONSHIP_META ->
          RelationshipMetaSupport.relationshipMetaLocation(property.jsonapiName());
    };
  }

  private static MappingDiagnostic diagnosticFor(PropertyRole role) {
    return switch (role) {
      case ID -> MappingDiagnostic.MISSING_IDENTIFIER;
      case ATTRIBUTE -> MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE;
      case RELATIONSHIP -> MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE;
      case RESOURCE_META, RELATIONSHIP_META -> MappingDiagnostic.INVALID_META_TARGET;
    };
  }

  private PropertyScopedValueConverter.SerializationResult convertAttributeValue(
      Object resource,
      ResourceMapping mapping,
      MappingProperty property,
      @Nullable Object rawValue) {
    try {
      return propertyScoped.serialize(
          mapping.domainType(),
          property.definition().getFullName().getSimpleName(),
          resource,
          rawValue,
          unwrapOptional(rawValue));
    } catch (RuntimeException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
          resource.getClass(),
          memberLocation(property, PropertyRole.ATTRIBUTE),
          "Failed to serialize attribute '" + property.logicalName() + "'",
          e);
    }
  }

  /**
   * Declared to-many target validation through the canonical configured-Jackson metadata authority:
   * the declared element type must carry resource metadata as the configured mapper sees it
   * (including class-level mix-ins). Presence-only, so absence keeps this path's stable diagnostic.
   */
  private void checkDeclaredTargetHasResourceMetadata(
      Class<?> rawType, MappingLocation relationshipLocation) {
    if (cache.findResourceTypeName(rawType) == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
          rawType,
          relationshipLocation,
          "Collection element type " + rawType.getName() + " lacks @JsonApiResource");
    }
  }

  private sealed interface ToManyClassification permits ResourceIdentifiers, DomainObjects, Mixed {}

  private record ResourceIdentifiers(List<ResourceIdentifier> identifiers)
      implements ToManyClassification {}

  private record DomainObjects(List<?> items) implements ToManyClassification {}

  private record Mixed(Object firstNonResourceIdentifier) implements ToManyClassification {}
}
