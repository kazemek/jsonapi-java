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
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.PropertyMetadata;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.SerializationContextExt;
import tools.jackson.databind.util.TokenBuffer;

/**
 * Binds a validated single-resource update into a {@link PatchCommand} without constructing a DTO.
 *
 * <p>Converts only supplied mapped attributes and relationships in attribute-then-relationship
 * encounter order. Reuses {@link ResourceMapping} definitions and the same relationship cardinality
 * / linkage rules as {@link RelationshipLinkageSupport}; attribute values use per-member {@link
 * JsonMapper#convertValue}. Document {@code included} is never read. A relationship member without
 * {@code data} produces no change; {@code readValue} rejects that shape earlier with {@code
 * RELATIONSHIP_DATA_REQUIRED}, while {@code fromDocument} skips it without re-validation.
 *
 * <p>Property-level {@code @JsonDeserialize} conversion uses the same TokenBuffer helpers as
 * Jackson's {@code convertValue} ({@code _deserializationContext()}, {@link
 * DeserializationContextExt}, {@link SerializationContextExt}). Those APIs are Jackson framework
 * conversion internals; this binder tracks the module's pinned Jackson 3.2.2 line.
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
  @SuppressWarnings("java:S1452")
  public PatchCommand<?> fromResource(ResourceObject resource, JavaType targetType) {
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(targetType, "targetType");
    Class<?> rawType = targetType.getRawClass();
    ResourceMapping mapping = cache.resolve(rawType);
    validateResourceType(resource, mapping, rawType);
    Object identity = convertIdentity(resource, mapping, rawType);
    List<PatchChange> changes = new ArrayList<>();
    bindAttributeChanges(resource, mapping, rawType, changes);
    bindRelationshipChanges(resource, mapping, changes);
    @SuppressWarnings({"rawtypes", "unchecked"})
    PatchCommand<?> command = new PatchCommand(rawType, identity, changes);
    return command;
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
      Object rawValue = entry.getValue();
      Object value = convertAttributeValue(property, rawValue, rawType);
      changes.add(
          new PatchChange.AttributeChange(property.jsonapiName(), property.logicalName(), value));
    }
  }

  /**
   * Per-member conversion via {@link JsonMapper#convertValue}, honoring property-level
   * {@code @JsonDeserialize} when present. Explicit JSON {@code null} is stored as {@code value ==
   * null} even when {@code convertValue} would produce {@code Optional.empty()}. Explicit null for
   * a primitive property is always {@link MappingDiagnostic#UNSUPPORTED_ATTRIBUTE_VALUE},
   * independent of the caller's {@code FAIL_ON_NULL_FOR_PRIMITIVES} setting.
   */
  private @Nullable Object convertAttributeValue(
      MappingProperty property, @Nullable Object rawValue, Class<?> rawType) {
    JavaType propertyType = property.accessor().getType();
    if (rawValue == null && propertyType.isPrimitive()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
          rawType,
          "/" + property.logicalName(),
          "Explicit null is not supported for primitive attribute '"
              + property.logicalName()
              + "' on "
              + rawType.getName());
    }
    try {
      Object converted = convertAttribute(property, rawValue, propertyType);
      return rawValue == null ? null : converted;
    } catch (RuntimeException e) {
      if (e instanceof JsonApiMappingException mappingException) {
        throw mappingException;
      }
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
          rawType,
          "/" + property.logicalName(),
          "Failed to convert attribute '" + property.logicalName() + "' for " + rawType.getName(),
          e);
    }
  }

  /**
   * Uses {@code convertValue} unless the property declares {@code @JsonDeserialize}, in which case
   * Jackson's TokenBuffer conversion helpers run that deserializer with a real context (same
   * machinery as {@code convertValue}, property-scoped).
   */
  private @Nullable Object convertAttribute(
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
    DeserializationContextExt context = mapper._deserializationContext();
    ValueDeserializer<Object> deserializer =
        contextualPropertyDeserializer(context, property, propertyType, deserializerDef);
    if (deserializer == null) {
      return mapper.convertValue(rawValue, propertyType);
    }
    try (JsonParser parser = conversionParser(context, rawValue)) {
      return deserializer.deserialize(parser, context);
    }
  }

  private static @Nullable ValueDeserializer<Object> contextualPropertyDeserializer(
      DeserializationContextExt context,
      MappingProperty property,
      JavaType propertyType,
      Object deserializerDef) {
    ValueDeserializer<Object> created =
        context.deserializerInstance(property.accessor(), deserializerDef);
    if (created == null) {
      return null;
    }
    @SuppressWarnings("unchecked")
    ValueDeserializer<Object> contextual =
        (ValueDeserializer<Object>)
            context.handlePrimaryContextualization(
                created, asBeanProperty(property, propertyType), propertyType);
    return contextual;
  }

  private static BeanProperty asBeanProperty(MappingProperty property, JavaType propertyType) {
    return new BeanProperty.Std(
        PropertyName.construct(property.logicalName()),
        propertyType,
        null,
        property.accessor(),
        PropertyMetadata.STD_OPTIONAL);
  }

  /**
   * TokenBuffer-backed parser positioned on the first value token (Jackson {@code convertValue}).
   */
  private JsonParser conversionParser(DeserializationContextExt context, Object rawValue) {
    SerializationContextExt serializationContext = mapper._serializationContext();
    TokenBuffer buffer = serializationContext.bufferForValueConversion();
    if (mapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
      buffer = buffer.forceUseOfBigDecimal(true);
    }
    serializationContext.serializeValue(buffer, rawValue);
    JsonParser parser = buffer.asParser(context);
    context.assignParser(parser);
    parser.nextToken();
    return parser;
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
          Object value = convertRelationship(property, data);
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
    Class<?> targetClass =
        RelationshipLinkageSupport.resolveTargetClass(propertyType, toMany, property);
    Object intermediate;
    if (targetClass == ResourceIdentifier.class) {
      intermediate = RelationshipLinkageSupport.builtInLinkage(property, data, toMany);
    } else {
      RelationshipLinkageMapper linkageMapper = linkageMappers.get(targetClass);
      if (linkageMapper == null) {
        throw RelationshipLinkageSupport.unsupportedRelationshipTarget(property, targetClass);
      }
      JavaType mapperTargetType =
          toMany ? propertyType : RelationshipLinkageSupport.unwrapOptionalType(propertyType);
      intermediate =
          RelationshipLinkageSupport.mappedLinkage(
              property, data, toMany, linkageMapper, mapperTargetType);
    }
    return finalizeRelationshipValue(intermediate, propertyType, property);
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
          RelationshipLinkageSupport.rawTypeOf(property),
          RelationshipLinkageSupport.relationshipPath(property),
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
      Object first = list.getFirst();
      return contentType.getRawClass().isInstance(first);
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
}
