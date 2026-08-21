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

  private final JsonMapper mapper;
  private final IdentifierConverter identifierConverter;
  private final MappingDefinitionCache cache;
  private final WholeMetaTarget wholeMetaTarget;

  public DomainResourceWriter(
      JsonMapper mapper, IdentifierConverter identifierConverter, MappingDefinitionCache cache) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.identifierConverter = Objects.requireNonNull(identifierConverter, "identifierConverter");
    this.cache = Objects.requireNonNull(cache, "cache");
    this.wholeMetaTarget = new WholeMetaTarget(mapper);
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
        throw new JsonApiMappingException(
            MappingDiagnostic.INVALID_FIELDSET_FIELD,
            resourceClass,
            name,
            "Unknown fieldset field '" + name + "' on " + mapping.resourceType());
      }
      if (!fieldPolicy.allows(mapping.resourceType(), name)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.DENIED_FIELDSET_FIELD,
            resourceClass,
            name,
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
      throw missingIdentifier(
          type, property, "Identifier property '" + property.logicalName() + "' is null");
    }
    String identifierString = identifierConverter.convert(identifierValue);
    if (identifierString == null) {
      throw missingIdentifier(
          type,
          property,
          "Identifier converter returned null for property '" + property.logicalName() + "'");
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

  static List<Object> convertToCollection(Object value) {
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
      default ->
          throw new JsonApiMappingException(
              MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
              value.getClass(),
              null,
              "To-many relationship value is not a supported collection type: "
                  + value.getClass().getName());
    };
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
          attributes.put(property.jsonapiName(), convertAttributeValue(unwrapOptional(rawValue)));
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
          property.jsonapiName(), buildRelationship(resource, property, relationshipMetaByTarget));
    }
    return Relationships.ofRelationships(relationships);
  }

  private Relationship buildRelationship(
      Object resource,
      MappingProperty property,
      Map<String, MappingProperty> relationshipMetaByTarget) {
    Object value = readValue(resource, property, PropertyRole.RELATIONSHIP);
    JavaType propertyType = property.accessor().getType();
    RelationshipData linkage =
        isToManyType(propertyType)
            ? extractToManyLinkage(value, propertyType)
            : extractToOneLinkage(value);
    Meta meta = null;
    MappingProperty metaProperty = relationshipMetaByTarget.get(property.jsonapiName());
    if (metaProperty != null) {
      meta =
          buildMetaValue(
              resource,
              metaProperty,
              RelationshipMetaSupport.relationshipMetaPath(property.jsonapiName()));
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
        resource, resourceMetaProperty, RelationshipMetaSupport.resourceMetaPath());
  }

  /**
   * Converts one whole-meta property value into a core {@link Meta}. The converted result must be a
   * {@link Map}; scalar/array/non-object runtime values fail with a stable meta diagnostic (never a
   * leaked cast or core-validation failure). Failures report the location-specific {@code path}.
   */
  private @Nullable Meta buildMetaValue(Object resource, MappingProperty property, String path) {
    Object rawValue = readValue(resource, property, property.role());
    Object value = unwrapOptional(rawValue);
    if (value == null) {
      return null;
    }
    Object converted;
    try {
      converted = mapper.convertValue(value, Object.class);
    } catch (RuntimeException e) {
      throw metaValueFailure(resource, path, "Failed to convert meta value", e);
    }
    if (!(converted instanceof Map<?, ?> map)) {
      throw metaValueFailure(
          resource,
          path,
          "Converted meta value is not an object (expected a JSON object, got "
              + convertedTypeName(converted)
              + ")",
          null);
    }
    try {
      return Meta.of(castMembers(map));
    } catch (JsonApiValidationException e) {
      throw metaValueFailure(resource, path, "Invalid meta members", e);
    }
  }

  private static String convertedTypeName(@Nullable Object converted) {
    return converted == null ? "null" : converted.getClass().getName();
  }

  /**
   * Rebuilds the converted meta value into a string-keyed member map. Today the conversion target
   * {@code Object.class} always yields string keys (Jackson's untyped map representation), so the
   * non-string branch is defensive: it keeps the stable {@link
   * MappingDiagnostic#INVALID_META_TARGET} diagnostic instead of leaking a class cast or a
   * core-validation failure if a future Jackson version ever emits non-string keys.
   */
  private static Map<String, Object> castMembers(Map<?, ?> map) {
    Map<String, Object> members = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      Object key = entry.getKey();
      if (!(key instanceof String stringKey)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.INVALID_META_TARGET,
            null,
            null,
            "Meta object key is not a string: " + key);
      }
      members.put(stringKey, entry.getValue());
    }
    return members;
  }

  private JsonApiMappingException metaValueFailure(
      Object resource, String path, String message, @Nullable Throwable cause) {
    return cause == null
        ? new JsonApiMappingException(
            MappingDiagnostic.INVALID_META_TARGET, resource.getClass(), path, message)
        : new JsonApiMappingException(
            MappingDiagnostic.INVALID_META_TARGET, resource.getClass(), path, message, cause);
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

  private RelationshipData extractToManyLinkage(@Nullable Object value, JavaType propType) {
    if (value == null) {
      return RelationshipData.IdentifierCollectionLinkage.empty();
    }
    List<Object> items = convertToCollection(value);
    if (items.isEmpty()) {
      return RelationshipData.IdentifierCollectionLinkage.empty();
    }
    return switch (classifyToManyItems(items)) {
      case ResourceIdentifiers(List<ResourceIdentifier> identifiers) ->
          new RelationshipData.IdentifierCollectionLinkage(identifiers);
      case DomainObjects(List<?> domainItems) ->
          toManyLinkageFromDomainObjects(domainItems, propType);
      case Mixed(Object firstNonResourceIdentifier) ->
          throw mixedToManyElements(firstNonResourceIdentifier);
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

  private RelationshipData toManyLinkageFromDomainObjects(List<?> items, JavaType propType) {
    JavaType contentType = resolveContentType(propType);
    if (contentType == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
          null,
          null,
          "Cannot resolve collection content type");
    }
    checkDeclaredTargetHasResourceMetadata(contentType.getRawClass());
    List<ResourceIdentifier> identifiers = new ArrayList<>(items.size());
    for (Object item : items) {
      identifiers.add(extractIdentifier(item));
    }
    return new RelationshipData.IdentifierCollectionLinkage(identifiers);
  }

  private static JsonApiMappingException missingIdentifier(
      Class<?> type, MappingProperty property, String message) {
    return new JsonApiMappingException(
        MappingDiagnostic.MISSING_IDENTIFIER, type, property.logicalName(), message);
  }

  private static JsonApiMappingException mixedToManyElements(Object firstNonResourceIdentifier) {
    return new JsonApiMappingException(
        MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
        firstNonResourceIdentifier.getClass(),
        null,
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
          property.logicalName(),
          "Failed to read property '"
              + property.logicalName()
              + "' ("
              + role.name().toLowerCase()
              + ")",
          e);
    }
  }

  private static MappingDiagnostic diagnosticFor(PropertyRole role) {
    return switch (role) {
      case ID -> MappingDiagnostic.MISSING_IDENTIFIER;
      case ATTRIBUTE -> MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE;
      case RELATIONSHIP -> MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE;
      case RESOURCE_META, RELATIONSHIP_META -> MappingDiagnostic.INVALID_META_TARGET;
    };
  }

  private @Nullable Object convertAttributeValue(@Nullable Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return value;
    }
    return mapper.convertValue(value, Object.class);
  }

  /**
   * Declared to-many target validation through the canonical configured-Jackson metadata authority:
   * the declared element type must carry resource metadata as the configured mapper sees it
   * (including class-level mix-ins). Presence-only, so absence keeps this path's stable diagnostic.
   */
  private void checkDeclaredTargetHasResourceMetadata(Class<?> rawType) {
    if (cache.findResourceTypeName(rawType) == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
          rawType,
          null,
          "Collection element type " + rawType.getName() + " lacks @JsonApiResource");
    }
  }

  private sealed interface ToManyClassification permits ResourceIdentifiers, DomainObjects, Mixed {}

  private record ResourceIdentifiers(List<ResourceIdentifier> identifiers)
      implements ToManyClassification {}

  private record DomainObjects(List<?> items) implements ToManyClassification {}

  private record Mixed(Object firstNonResourceIdentifier) implements ToManyClassification {}
}
