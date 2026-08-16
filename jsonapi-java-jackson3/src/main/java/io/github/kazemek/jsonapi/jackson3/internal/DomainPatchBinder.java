package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchChange;
import io.github.kazemek.jsonapi.jackson.PatchCommand;
import io.github.kazemek.jsonapi.jackson3.RelationshipLinkageMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.PropertyMetadata;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.json.JsonMapper;

/**
 * Binds a validated single-resource update into a {@link PatchCommand} without constructing a DTO.
 *
 * <p>Converts only supplied mapped attributes and relationships in attribute-then-relationship
 * encounter order. Reuses {@link ResourceMapping} definitions and the same relationship cardinality
 * / linkage rules as {@link DomainResourceBinder}; attribute values use per-member {@link
 * JsonMapper#convertValue}. Document {@code included} is never read.
 */
public final class DomainPatchBinder {

  private static final String IDENTIFIER_PATH_ID = "/id";

  private final JsonMapper mapper;
  private final IdentifierConverter identifierConverter;
  private final MappingDefinitionCache cache;
  private final Map<Class<?>, RelationshipLinkageMapper> linkageMappers;

  public DomainPatchBinder(
      JsonMapper mapper,
      IdentifierConverter identifierConverter,
      MappingDefinitionCache cache,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.identifierConverter = Objects.requireNonNull(identifierConverter, "identifierConverter");
    this.cache = Objects.requireNonNull(cache, "cache");
    this.linkageMappers = Map.copyOf(Objects.requireNonNull(linkageMappers, "linkageMappers"));
  }

  /** Binds one resource object into a presence-aware patch command for {@code targetType}. */
  @SuppressWarnings("unchecked")
  public <T> PatchCommand<T> fromResource(ResourceObject resource, JavaType targetType) {
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(targetType, "targetType");
    Class<?> rawType = targetType.getRawClass();
    ResourceMapping mapping = cache.resolve(rawType);
    validateResourceType(resource, mapping, rawType);
    Object identity = convertIdentity(resource, mapping, rawType);
    List<PatchChange> changes = new ArrayList<>();
    bindAttributeChanges(resource, mapping, rawType, changes);
    bindRelationshipChanges(resource, mapping, changes);
    return new PatchCommand<>((Class<T>) rawType, identity, changes);
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

  private Object convertIdentity(
      ResourceObject resource, ResourceMapping mapping, Class<?> rawType) {
    MappingProperty identifierProperty = mapping.identifierProperty();
    if (identifierProperty == null || !resource.hasId()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED,
          rawType,
          IDENTIFIER_PATH_ID,
          "Resource update identity requires a non-null id at '" + IDENTIFIER_PATH_ID + "'");
    }
    String wireIdentifier = Objects.requireNonNull(resource.id());
    Object parsed;
    try {
      parsed = identifierConverter.parse(wireIdentifier);
    } catch (RuntimeException e) {
      throw identifierConversionFailed(rawType, e);
    }
    if (parsed == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED,
          rawType,
          IDENTIFIER_PATH_ID,
          "Identifier converter returned null for the wire identifier at '"
              + IDENTIFIER_PATH_ID
              + "'");
    }
    try {
      Object converted = mapper.convertValue(parsed, identifierProperty.accessor().getType());
      return Objects.requireNonNull(converted, "identity");
    } catch (RuntimeException e) {
      throw identifierConversionFailed(rawType, e);
    }
  }

  private static JsonApiMappingException identifierConversionFailed(
      Class<?> rawType, Throwable cause) {
    return new JsonApiMappingException(
        MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED,
        rawType,
        IDENTIFIER_PATH_ID,
        "Failed to convert the wire identifier at '"
            + IDENTIFIER_PATH_ID
            + "' for "
            + rawType.getName(),
        cause);
  }

  private void bindAttributeChanges(
      ResourceObject resource,
      ResourceMapping mapping,
      Class<?> rawType,
      List<PatchChange> changes) {
    Attributes attributes = resource.attributes();
    if (attributes == null || mapping.attributes().isEmpty()) {
      return;
    }
    Map<String, MappingProperty> byJsonapiName = byJsonapiName(mapping.attributes());
    for (Map.Entry<String, @Nullable Object> entry : attributes.attributes().entrySet()) {
      MappingProperty property = byJsonapiName.get(entry.getKey());
      if (property == null) {
        continue;
      }
      @Nullable Object rawValue = entry.getValue();
      @Nullable Object value = convertAttributeValue(property, rawValue, rawType);
      changes.add(
          new PatchChange.AttributeChange(property.jsonapiName(), property.logicalName(), value));
    }
  }

  /**
   * Per-member conversion via {@link JsonMapper#convertValue}, honoring property-level
   * {@code @JsonDeserialize} when present. Explicit JSON {@code null} is stored as {@code value ==
   * null} even when {@code convertValue} would produce {@code Optional.empty()}.
   */
  private @Nullable Object convertAttributeValue(
      MappingProperty property, @Nullable Object rawValue, Class<?> rawType) {
    JavaType propertyType = property.accessor().getType();
    try {
      Object converted = convertAttributeWithOptionalDeserializer(property, rawValue, propertyType);
      return rawValue == null ? null : converted;
    } catch (RuntimeException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
          rawType,
          "/" + property.logicalName(),
          "Failed to convert attribute '" + property.logicalName() + "' for " + rawType.getName(),
          e);
    }
  }

  /**
   * Honors property-level {@code @JsonDeserialize} via the mapper's {@link
   * tools.jackson.databind.DeserializationContext} (contextualized against the property {@link
   * tools.jackson.databind.introspect.AnnotatedMember}). Falls back to {@link
   * JsonMapper#convertValue} when no property deserializer is declared.
   */
  private @Nullable Object convertAttributeWithOptionalDeserializer(
      MappingProperty property, @Nullable Object rawValue, JavaType propertyType) {
    DeserializationConfig config = mapper.deserializationConfig();
    Object deserializerDef =
        config.getAnnotationIntrospector().findDeserializer(config, property.accessor());
    if (deserializerDef == null) {
      return mapper.convertValue(rawValue, propertyType);
    }
    if (rawValue == null) {
      return null;
    }
    DeserializationContextExt ctxt = mapper._deserializationContext();
    ValueDeserializer<Object> deserializer =
        ctxt.deserializerInstance(property.accessor(), deserializerDef);
    if (deserializer == null) {
      return mapper.convertValue(rawValue, propertyType);
    }
    BeanProperty beanProperty =
        new BeanProperty.Std(
            PropertyName.construct(property.logicalName()),
            propertyType,
            null,
            property.accessor(),
            PropertyMetadata.STD_OPTIONAL);
    @SuppressWarnings("unchecked")
    ValueDeserializer<Object> contextual =
        (ValueDeserializer<Object>)
            ctxt.handlePrimaryContextualization(deserializer, beanProperty, propertyType);
    try (JsonParser parser = mapper.createParser(mapper.writeValueAsBytes(rawValue))) {
      parser.nextToken();
      return contextual.deserialize(parser, ctxt.assignParser(parser));
    }
  }

  private void bindRelationshipChanges(
      ResourceObject resource, ResourceMapping mapping, List<PatchChange> changes) {
    Relationships relationships = resource.relationships();
    if (relationships == null || mapping.relationships().isEmpty()) {
      return;
    }
    Map<String, MappingProperty> byJsonapiName = byJsonapiName(mapping.relationships());
    for (Map.Entry<String, Relationship> entry : relationships.relationships().entrySet()) {
      MappingProperty property = byJsonapiName.get(entry.getKey());
      if (property != null) {
        RelationshipData data = entry.getValue().data();
        if (data != null) {
          @Nullable Object value = convertRelationship(property, data);
          changes.add(
              new PatchChange.RelationshipChange(
                  property.jsonapiName(), property.logicalName(), value));
        }
      }
    }
  }

  private static Map<String, MappingProperty> byJsonapiName(List<MappingProperty> properties) {
    Map<String, MappingProperty> byName = new LinkedHashMap<>();
    for (MappingProperty property : properties) {
      byName.put(property.jsonapiName(), property);
    }
    return byName;
  }

  private @Nullable Object convertRelationship(MappingProperty property, RelationshipData data) {
    JavaType propertyType = property.accessor().getType();
    boolean toMany = DomainResourceWriter.isToManyType(propertyType);
    Class<?> targetClass = resolveTargetClass(propertyType, toMany, property);
    @Nullable Object intermediate;
    if (targetClass == ResourceIdentifier.class) {
      intermediate = builtInLinkage(property, data, toMany);
    } else {
      RelationshipLinkageMapper linkageMapper = linkageMappers.get(targetClass);
      if (linkageMapper == null) {
        throw unsupportedRelationshipTarget(property, targetClass);
      }
      JavaType mapperTargetType = toMany ? propertyType : unwrapOptionalType(propertyType);
      intermediate = mappedLinkage(property, data, toMany, linkageMapper, mapperTargetType);
    }
    return finalizeRelationshipValue(intermediate, propertyType, property);
  }

  private static Class<?> resolveTargetClass(
      JavaType propertyType, boolean toMany, MappingProperty property) {
    if (toMany) {
      JavaType contentType = DomainResourceWriter.resolveContentType(propertyType);
      if (contentType == null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET,
            rawTypeOf(property),
            relationshipPath(property),
            "Cannot resolve collection content type for relationship '"
                + property.logicalName()
                + "'");
      }
      return contentType.getRawClass();
    }
    return unwrapOptionalType(propertyType).getRawClass();
  }

  private static @Nullable Object builtInLinkage(
      MappingProperty property, RelationshipData data, boolean toMany) {
    boolean empty = validateCardinality(property, data, toMany);
    return switch (data) {
      case RelationshipData.NullLinkage ignored -> null;
      case RelationshipData.SingleLinkage(ResourceIdentifier identifier) -> linkageMap(identifier);
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers) -> {
        if (empty) {
          yield List.of();
        }
        List<Object> values = new ArrayList<>(identifiers.size());
        for (ResourceIdentifier identifier : identifiers) {
          values.add(linkageMap(identifier));
        }
        yield values;
      }
    };
  }

  private @Nullable Object mappedLinkage(
      MappingProperty property,
      RelationshipData data,
      boolean toMany,
      RelationshipLinkageMapper linkageMapper,
      JavaType mapperTargetType) {
    boolean empty = validateCardinality(property, data, toMany);
    return switch (data) {
      case RelationshipData.NullLinkage ignored -> null;
      case RelationshipData.SingleLinkage single ->
          invokeLinkageMapper(linkageMapper, single, mapperTargetType, property);
      case RelationshipData.IdentifierCollectionLinkage collection -> {
        if (empty) {
          yield List.of();
        }
        yield invokeLinkageMapper(linkageMapper, collection, mapperTargetType, property);
      }
    };
  }

  private @Nullable Object finalizeRelationshipValue(
      @Nullable Object intermediate, JavaType propertyType, MappingProperty property) {
    if (alreadyConverted(intermediate, propertyType)) {
      return intermediate;
    }
    try {
      return mapper.convertValue(intermediate, propertyType);
    } catch (RuntimeException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET,
          rawTypeOf(property),
          relationshipPath(property),
          "Failed to convert relationship '"
              + property.logicalName()
              + "' to "
              + propertyType.toCanonical(),
          e);
    }
  }

  /**
   * True when {@code intermediate} is already a usable property value (for example a linkage-mapper
   * result) and does not need {@code convertValue} coercion to List/Set/array/Optional shapes.
   */
  private static boolean alreadyConverted(@Nullable Object intermediate, JavaType propertyType) {
    if (intermediate == null || propertyType.isTypeOrSubTypeOf(Optional.class)) {
      return false;
    }
    if (DomainResourceWriter.isToManyType(propertyType)) {
      if (!(intermediate instanceof List<?> list)) {
        return propertyType.getRawClass().isInstance(intermediate)
            && !needsCollectionCoercion(intermediate, propertyType);
      }
      if (needsCollectionCoercion(intermediate, propertyType)) {
        return false;
      }
      if (list.isEmpty()) {
        return propertyType.getRawClass().isInstance(intermediate);
      }
      JavaType contentType = DomainResourceWriter.resolveContentType(propertyType);
      if (contentType == null) {
        return false;
      }
      Object first = list.get(0);
      return first != null && contentType.getRawClass().isInstance(first);
    }
    return propertyType.getRawClass().isInstance(intermediate);
  }

  private static boolean needsCollectionCoercion(Object intermediate, JavaType propertyType) {
    if (!DomainResourceWriter.isToManyType(propertyType)) {
      return false;
    }
    if (propertyType.isArrayType()) {
      return true;
    }
    return propertyType.isTypeOrSubTypeOf(Set.class) && !(intermediate instanceof Set);
  }

  private static boolean validateCardinality(
      MappingProperty property, RelationshipData data, boolean toMany) {
    return switch (data) {
      case RelationshipData.NullLinkage ignored -> {
        if (toMany) {
          throw cardinalityMismatch(property, "null linkage on to-many relationship");
        }
        yield true;
      }
      case RelationshipData.SingleLinkage ignored -> {
        if (toMany) {
          throw cardinalityMismatch(property, "single linkage on to-many relationship");
        }
        yield false;
      }
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers) -> {
        boolean empty = identifiers.isEmpty();
        if (!toMany) {
          throw cardinalityMismatch(
              property,
              empty
                  ? "empty collection linkage on to-one relationship"
                  : "collection linkage on to-one relationship");
        }
        yield empty;
      }
    };
  }

  private static @Nullable Object invokeLinkageMapper(
      RelationshipLinkageMapper linkageMapper,
      RelationshipData data,
      JavaType targetType,
      MappingProperty property) {
    try {
      return linkageMapper.map(data, targetType);
    } catch (RuntimeException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.LINKAGE_MAPPING_FAILED,
          rawTypeOf(property),
          relationshipPath(property),
          "Relationship linkage mapper failed for relationship '" + property.logicalName() + "'",
          e);
    }
  }

  private static Map<String, @Nullable Object> linkageMap(ResourceIdentifier identifier) {
    Map<String, @Nullable Object> linkage = new LinkedHashMap<>();
    linkage.put("type", identifier.type());
    linkage.put("id", identifier.id());
    linkage.put("lid", identifier.lid());
    return linkage;
  }

  private static JavaType unwrapOptionalType(JavaType type) {
    if (type.isTypeOrSubTypeOf(Optional.class) && type.containedTypeCount() == 1) {
      return type.containedType(0);
    }
    return type;
  }

  private static Class<?> rawTypeOf(MappingProperty property) {
    return property.accessor().getType().getRawClass();
  }

  private static JsonApiMappingException cardinalityMismatch(
      MappingProperty property, String detail) {
    return new JsonApiMappingException(
        MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
        rawTypeOf(property),
        relationshipPath(property),
        "Cardinality mismatch for relationship '" + property.logicalName() + "': " + detail);
  }

  private static JsonApiMappingException unsupportedRelationshipTarget(
      MappingProperty property, Class<?> targetClass) {
    return new JsonApiMappingException(
        MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET,
        rawTypeOf(property),
        relationshipPath(property),
        "Relationship '"
            + property.logicalName()
            + "' targets unsupported type "
            + targetClass.getName());
  }

  private static String relationshipPath(MappingProperty property) {
    return "/relationships/" + property.jsonapiName() + "/data";
  }
}
