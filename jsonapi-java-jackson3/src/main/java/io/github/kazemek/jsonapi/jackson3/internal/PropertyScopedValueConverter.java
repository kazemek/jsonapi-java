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
   * TokenBuffer-backed parser positioned on the first value token (Jackson {@code convertValue}). A
   * {@code null} {@code rawValue} serializes to a {@link tools.jackson.core.JsonToken#VALUE_NULL}
   * token so the property deserializer's null handling applies.
   */
  private JsonParser conversionParser(
      DeserializationContextExt context, @Nullable Object rawValue) {
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
