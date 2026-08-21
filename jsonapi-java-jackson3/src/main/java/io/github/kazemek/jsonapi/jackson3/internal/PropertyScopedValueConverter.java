package io.github.kazemek.jsonapi.jackson3.internal;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.bean.BeanDeserializerBase;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.BeanPropertyWriter;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.SerializationContextExt;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import tools.jackson.databind.util.TokenBuffer;

/**
 * Location-neutral property-scoped Jackson conversion authority for one member value.
 *
 * <p>Shared by {@link DomainResourceWriter} (ordinary mapped writes), {@link PatchMemberConverter}
 * (top-level attributes and identifiers), and {@link StructuredValueBinder} (low-level nested
 * atomic members) so the locations cannot silently drift on which configured Jackson authority
 * applies to a supplied member. It has no {@link ResourceMapping} / {@link MappingProperty} /
 * {@code @JsonApiAttribute} / {@link io.github.kazemek.jsonapi.jackson.PatchChange} / location
 * dependency: callers supply the containing bean's {@link JavaType}, the member's Jackson-resolved
 * wire name, the conversion-target {@link JavaType}, and the raw wire value, so a later structured
 * JSON:API {@code meta} mapping can reuse the same machinery at its own location (ADR-014, KAZ-77
 * reuse boundary).
 *
 * <p>The member's fully-contextualized property is resolved from the containing bean's {@link
 * BeanDeserializerBase} (the same {@link SettableBeanProperty} Jackson would use during normal
 * binding) and the value converts through {@link SettableBeanProperty#deserialize}, so all
 * property-scoped deserialization semantics are honored exactly as Jackson applies them —
 * {@code @JsonDeserialize using / contentUsing / keyUsing}, deserialization converters, type
 * refinement (including a property-level {@code TypeDeserializer} for polymorphic values), and the
 * property's null provider — regardless of whether the annotation sits on a field, accessor, or
 * creator parameter. When no bean-based property can be resolved, the value falls back to ordinary
 * {@code convertValue}, which still applies type-level and module authority. A null {@code
 * rawValue} converts through the property's null value (for example {@code Optional.empty()} for an
 * {@link java.util.Optional} target).
 *
 * <p>On write, the member's fully-contextualized {@link BeanPropertyWriter} is resolved from the
 * containing bean's {@link BeanSerializerBase}. Its configured serializer writes the already-read
 * value into a conversion buffer without a property name, and the resulting tokens are read back as
 * an untyped JSON-compatible value. This is deliberately the normal property serializer path rather
 * than manual annotation extraction, so property serializers, mix-ins, contextual serializers, type
 * serializers, content serializers, and mapper modules remain configured-Jackson authority without
 * reading an accessor twice.
 */
final class PropertyScopedValueConverter {

  private final JsonMapper mapper;
  private final JsonMapper serializationMapper;

  PropertyScopedValueConverter(JsonMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.serializationMapper =
        mapper.isEnabled(SerializationFeature.WRAP_ROOT_VALUE)
            ? mapper.rebuild().disable(SerializationFeature.WRAP_ROOT_VALUE).build()
            : mapper;
  }

  /**
   * Converts {@code rawValue} for a single member of the containing bean {@code beanType},
   * identified by its Jackson-resolved {@code wireName}. {@code declaredType} is the member's
   * declared type and {@code targetType} the effective conversion target. When the caller unwraps a
   * {@code PatchPresence} wrapper ({@code declaredType != targetType}), the bean property
   * deserializer (which targets the declared {@code PatchPresence} type) is not applicable and the
   * value converts against {@code targetType} directly.
   */
  @Nullable Object convert(
      JavaType beanType,
      String wireName,
      JavaType declaredType,
      JavaType targetType,
      @Nullable Object rawValue) {
    SettableBeanProperty property = null;
    if (declaredType.equals(targetType)) {
      property = matchingProperty(beanType, wireName);
    }
    if (property == null) {
      return mapper.convertValue(rawValue, targetType);
    }
    DeserializationContextExt context = mapper._deserializationContext();
    try (JsonParser parser = conversionParser(context, rawValue)) {
      return property.deserialize(parser, context);
    }
  }

  /**
   * Serializes one mapped property through its configured Jackson property writer.
   *
   * <p>{@code rawValue} is the value already read from the mapped property. {@code fallbackValue}
   * is used only when no property writer can be resolved; callers pass the JSON:API-unwrapped value
   * there to preserve the adapter's existing Optional semantics. When a writer is available, its
   * fully contextualized serializer writes {@code rawValue} without reading the bean accessor a
   * second time.
   */
  @Nullable Object serialize(
      JavaType beanType,
      String wireName,
      @Nullable Object rawValue,
      @Nullable Object fallbackValue) {
    BeanPropertyWriter property = matchingSerializerProperty(beanType, wireName);
    if (property == null) {
      return mapper.convertValue(fallbackValue, Object.class);
    }
    SerializationContextExt serializationContext = serializationMapper._serializationContext();
    TokenBuffer buffer = serializationContext.bufferForValueConversion();
    if (mapper.isEnabled(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)) {
      buffer = buffer.forceUseOfBigDecimal(true);
    }
    try {
      serializePropertyValue(property, rawValue, buffer, serializationContext);
      DeserializationContextExt deserializationContext = mapper._deserializationContext();
      try (JsonParser parser = buffer.asParser(deserializationContext)) {
        deserializationContext.assignParser(parser);
        if (parser.nextToken() == null) {
          return null;
        }
        return deserializationContext.readValue(parser, Object.class);
      }
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to serialize property '" + wireName + "' through Jackson", e);
    }
  }

  @SuppressWarnings("DataFlowIssue")
  private static void serializePropertyValue(
      BeanPropertyWriter property,
      @Nullable Object rawValue,
      TokenBuffer buffer,
      SerializationContextExt context) {
    if (rawValue == null) {
      Object nullSerializerDefinition =
          context
              .getAnnotationIntrospector()
              .findNullSerializer(context.getConfig(), property.getMember());
      ValueSerializer<Object> nullSerializer =
          nullSerializerDefinition == null
              ? context.findNullValueSerializer(property)
              : context.serializerInstance(property.getMember(), nullSerializerDefinition);
      nullSerializer.serialize(null, buffer, context);
      return;
    }
    ValueSerializer<Object> serializer = property.getSerializer();
    if (serializer == null || context.isUnknownTypeSerializer(serializer)) {
      JavaType type = property.getType();
      if (type.getRawClass() == Object.class
          || type.isAbstract()
          || type.isInterface()
          || !type.getRawClass().isInstance(rawValue)) {
        type = context.constructSpecializedType(type, rawValue.getClass());
      }
      serializer = context.findPrimaryPropertySerializer(type, property);
    }
    if (property.getTypeSerializer() == null) {
      serializer.serialize(rawValue, buffer, context);
    } else {
      serializer.serializeWithType(rawValue, buffer, context, property.getTypeSerializer());
    }
  }

  /**
   * Resolves the fully-contextualized {@link SettableBeanProperty} from the containing bean, or
   * {@code null} when the bean has no property-driven deserializer or the wire-named property is
   * absent.
   */
  private @Nullable SettableBeanProperty matchingProperty(JavaType beanType, String wireName) {
    DeserializationContextExt context = mapper._deserializationContext();
    ValueDeserializer<Object> root = context.findRootValueDeserializer(beanType);
    if (!(root instanceof BeanDeserializerBase bean)) {
      return null;
    }
    return bean.findProperty(PropertyName.construct(wireName));
  }

  /**
   * Resolves the fully-contextualized property writer from the containing bean, or {@code null}
   * when the bean has no ordinary bean serializer or the wire-named property is absent.
   */
  private @Nullable BeanPropertyWriter matchingSerializerProperty(
      JavaType beanType, String wireName) {
    SerializationContextExt context = serializationMapper._serializationContext();
    ValueSerializer<Object> root = context.findRootValueSerializer(beanType);
    if (!(root instanceof BeanSerializerBase bean)) {
      return null;
    }
    for (var iterator = bean.properties(); iterator.hasNext(); ) {
      PropertyWriter writer = iterator.next();
      if (writer instanceof BeanPropertyWriter property && property.getName().equals(wireName)) {
        return property;
      }
    }
    return null;
  }

  /**
   * TokenBuffer-backed parser positioned on the first value token (Jackson {@code convertValue}). A
   * {@code null} {@code rawValue} serializes to a {@link tools.jackson.core.JsonToken#VALUE_NULL}
   * token so the property deserializer's null handling applies.
   */
  private JsonParser conversionParser(
      DeserializationContextExt context, @Nullable Object rawValue) {
    SerializationContextExt serializationContext = serializationMapper._serializationContext();
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
}
