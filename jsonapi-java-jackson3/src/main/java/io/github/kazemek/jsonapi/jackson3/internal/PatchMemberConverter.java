package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchCommand;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import io.github.kazemek.jsonapi.jackson3.RelationshipLinkageMapper;
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
 * Shared per-member conversion for the low-level {@link PatchCommand} path and the direct typed
 * PATCH DTO path, so the two cannot silently drift.
 *
 * <p>All conversion runs against an explicit conversion-target {@link JavaType}: the property
 * accessor type on the low-level path, and the single {@link PatchPresence} type argument (the
 * unwrapped inner type) on the DTO path. To-many detection, property-level {@code @JsonDeserialize}
 * handling, primitive-null rules, linkage resolution, and final collection coercion all use that
 * target type.
 */
final class PatchMemberConverter {

  private static final String IDENTIFIER_PATH_ID = "/id";

  /**
   * Null policy for attribute conversion: {@link #RAW_NULL} stores explicit JSON {@code null} as
   * raw Java {@code null} (low-level {@code PatchCommand} contract); {@link #CONVERT_THROUGH}
   * converts explicit JSON {@code null} through the inner type first (for example {@code
   * Optional.empty()}), which the DTO path wraps in {@code Present}.
   */
  enum AttributeNullPolicy {
    RAW_NULL,
    CONVERT_THROUGH
  }

  private final JsonMapper mapper;
  private final IdentifierConverter identifierConverter;
  private final Map<Class<?>, RelationshipLinkageMapper> linkageMappers;

  PatchMemberConverter(
      JsonMapper mapper,
      IdentifierConverter identifierConverter,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.identifierConverter = Objects.requireNonNull(identifierConverter, "identifierConverter");
    this.linkageMappers = Map.copyOf(Objects.requireNonNull(linkageMappers, "linkageMappers"));
  }

  /** Converts a wire identifier into the identifier property's converted value (never null). */
  Object convertIdentity(
      String wireIdentifier, MappingProperty identifierProperty, Class<?> rawType) {
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

  /**
   * Converts one supplied attribute value against {@code targetType}. In {@link
   * AttributeNullPolicy#RAW_NULL} mode explicit JSON {@code null} becomes raw Java {@code null}; in
   * {@link AttributeNullPolicy#CONVERT_THROUGH} mode it is converted through the inner type first.
   * Explicit null for a primitive target is always {@link
   * MappingDiagnostic#UNSUPPORTED_ATTRIBUTE_VALUE}, independent of the caller's {@code
   * FAIL_ON_NULL_FOR_PRIMITIVES} setting.
   */
  @Nullable Object convertAttribute(
      MappingProperty property,
      @Nullable Object rawValue,
      JavaType targetType,
      AttributeNullPolicy nullPolicy,
      Class<?> rawType) {
    if (rawValue == null && targetType.isPrimitive()) {
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
      if (rawValue == null && nullPolicy == AttributeNullPolicy.RAW_NULL) {
        return null;
      }
      return convertAttributeViaJackson(property, rawValue, targetType);
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
   * machinery as {@code convertValue}, property-scoped). A null {@code rawValue} converts through
   * the target type's null value (for example {@code Optional.empty()} for an {@link Optional}
   * target).
   */
  private @Nullable Object convertAttributeViaJackson(
      MappingProperty property, @Nullable Object rawValue, JavaType targetType) {
    DeserializationConfig config = mapper.deserializationConfig();
    Object deserializerDef =
        config.getAnnotationIntrospector().findDeserializer(config, property.accessor());
    if (deserializerDef == null || rawValue == null) {
      return mapper.convertValue(rawValue, targetType);
    }
    DeserializationContextExt context = mapper._deserializationContext();
    ValueDeserializer<Object> deserializer =
        contextualPropertyDeserializer(context, property, targetType, deserializerDef);
    if (deserializer == null) {
      return mapper.convertValue(rawValue, targetType);
    }
    try (JsonParser parser = conversionParser(context, rawValue)) {
      return deserializer.deserialize(parser, context);
    }
  }

  private static @Nullable ValueDeserializer<Object> contextualPropertyDeserializer(
      DeserializationContextExt context,
      MappingProperty property,
      JavaType targetType,
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
                created, asBeanProperty(property, targetType), targetType);
    return contextual;
  }

  private static BeanProperty asBeanProperty(MappingProperty property, JavaType targetType) {
    return new BeanProperty.Std(
        PropertyName.construct(property.logicalName()),
        targetType,
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

  /**
   * Converts one relationship linkage against {@code targetType} (the unwrapped inner type on the
   * DTO path).
   */
  @Nullable Object convertRelationship(
      MappingProperty property, RelationshipData data, JavaType targetType) {
    boolean toMany = DomainResourceWriter.isToManyType(targetType);
    Class<?> targetClass =
        RelationshipLinkageSupport.resolveTargetClass(targetType, toMany, property);
    Object intermediate;
    if (targetClass == ResourceIdentifier.class) {
      intermediate = RelationshipLinkageSupport.builtInLinkage(property, data, toMany);
    } else {
      RelationshipLinkageMapper linkageMapper = linkageMappers.get(targetClass);
      if (linkageMapper == null) {
        throw RelationshipLinkageSupport.unsupportedRelationshipTarget(property, targetClass);
      }
      JavaType mapperTargetType =
          toMany ? targetType : RelationshipLinkageSupport.unwrapOptionalType(targetType);
      intermediate =
          RelationshipLinkageSupport.mappedLinkage(
              property, data, toMany, linkageMapper, mapperTargetType);
    }
    return finalizeRelationshipValue(intermediate, targetType, property);
  }

  private @Nullable Object finalizeRelationshipValue(
      @Nullable Object intermediate, JavaType targetType, MappingProperty property) {
    if (alreadyConverted(intermediate, targetType)) {
      return intermediate;
    }
    try {
      return mapper.convertValue(intermediate, targetType);
    } catch (RuntimeException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET,
          RelationshipLinkageSupport.rawTypeOf(property),
          RelationshipLinkageSupport.relationshipPath(property),
          "Failed to convert relationship '"
              + property.logicalName()
              + "' to "
              + targetType.toCanonical(),
          e);
    }
  }

  /**
   * True when {@code intermediate} is already a usable property value (for example a linkage-mapper
   * result) and does not need {@code convertValue} coercion to List/Set/array/Optional shapes.
   */
  private static boolean alreadyConverted(@Nullable Object intermediate, JavaType targetType) {
    if (intermediate == null || targetType.isTypeOrSubTypeOf(Optional.class)) {
      return false;
    }
    if (DomainResourceWriter.isToManyType(targetType)) {
      if (!(intermediate instanceof List<?> list)) {
        return targetType.getRawClass().isInstance(intermediate)
            && !needsCollectionCoercion(intermediate, targetType);
      }
      if (needsCollectionCoercion(intermediate, targetType)) {
        return false;
      }
      if (list.isEmpty()) {
        return targetType.getRawClass().isInstance(intermediate);
      }
      JavaType contentType = DomainResourceWriter.resolveContentType(targetType);
      if (contentType == null) {
        return false;
      }
      Object first = list.getFirst();
      return contentType.getRawClass().isInstance(first);
    }
    return targetType.getRawClass().isInstance(intermediate);
  }

  private static boolean needsCollectionCoercion(Object intermediate, JavaType targetType) {
    if (!DomainResourceWriter.isToManyType(targetType)) {
      return false;
    }
    if (targetType.isArrayType()) {
      return true;
    }
    return targetType.isTypeOrSubTypeOf(Set.class) && !(intermediate instanceof Set);
  }

  static Map<String, MappingProperty> byJsonapiName(List<MappingProperty> properties) {
    Map<String, MappingProperty> byName = new LinkedHashMap<>();
    for (MappingProperty property : properties) {
      byName.put(property.jsonapiName(), property);
    }
    return byName;
  }
}
