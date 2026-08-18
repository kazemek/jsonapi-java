package io.github.kazemek.jsonapi.jackson3.internal;

import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.PropertyName;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationContextExt;
import tools.jackson.databind.deser.SettableBeanProperty;
import tools.jackson.databind.deser.bean.BeanDeserializerBase;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.SerializationContextExt;
import tools.jackson.databind.util.TokenBuffer;

/**
 * Location-neutral property-scoped Jackson deserialization authority for one member value.
 *
 * <p>Shared by {@link PatchMemberConverter} (top-level attributes) and {@link
 * StructuredValueBinder} (low-level nested atomic members) so the two cannot silently drift on
 * which configured Jackson authority applies to a supplied member. It has no {@link
 * ResourceMapping} / {@link MappingProperty} / {@code @JsonApiAttribute} / {@link
 * io.github.kazemek.jsonapi.jackson.PatchChange} / location dependency: callers supply the
 * containing bean's {@link JavaType}, the member's Jackson-resolved wire name, the
 * conversion-target {@link JavaType}, and the raw wire value, so a later structured JSON:API {@code
 * meta} mapping can reuse the same machinery at its own location (ADR-014, KAZ-77 reuse boundary).
 *
 * <p>The member's fully-contextualized property deserializer is resolved from the containing bean's
 * {@link BeanDeserializerBase} (the same {@link SettableBeanProperty} Jackson would use during
 * normal binding), so all property-scoped deserialization customization is honored exactly as
 * Jackson applies it — {@code @JsonDeserialize using / contentUsing / keyUsing}, deserialization
 * converters, and type refinement, regardless of whether the annotation sits on a field, accessor,
 * or creator parameter. When no bean-based property deserializer can be resolved, the value falls
 * back to ordinary {@code convertValue}, which still applies type-level and module authority. A
 * null {@code rawValue} converts through the target type's null value (for example {@code
 * Optional.empty()} for an {@link java.util.Optional} target).
 */
final class PropertyScopedValueConverter {

  private final JsonMapper mapper;

  PropertyScopedValueConverter(JsonMapper mapper) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
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
    if (rawValue == null) {
      return mapper.convertValue(null, targetType);
    }
    ValueDeserializer<Object> deserializer = null;
    if (declaredType.equals(targetType)) {
      deserializer = propertyDeserializer(beanType, wireName);
    }
    if (deserializer == null) {
      return mapper.convertValue(rawValue, targetType);
    }
    DeserializationContextExt context = mapper._deserializationContext();
    try (JsonParser parser = conversionParser(context, rawValue)) {
      return deserializer.deserialize(parser, context);
    }
  }

  /**
   * Resolves the fully-contextualized property value deserializer from the containing bean, or
   * {@code null} when the bean has no property-driven deserializer or the wire-named property is
   * absent.
   */
  private @Nullable ValueDeserializer<Object> propertyDeserializer(
      JavaType beanType, String wireName) {
    DeserializationContextExt context = mapper._deserializationContext();
    @SuppressWarnings("unchecked")
    ValueDeserializer<Object> root =
        (ValueDeserializer<Object>) context.findRootValueDeserializer(beanType);
    if (!(root instanceof BeanDeserializerBase bean)) {
      return null;
    }
    SettableBeanProperty property = bean.findProperty(PropertyName.construct(wireName));
    return property == null ? null : property.getValueDeserializer();
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
}
