package io.github.kazemek.jsonapi.jackson3.internal;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.json.JsonMapper;

/**
 * Detects wrapper-level Jackson serialization/deserialization customization on a member.
 *
 * <p>Shared by the typed PATCH DTO declaration validation and the structured-value engine so the
 * two cannot silently drift on what counts as wrapper-level {@code @JsonDeserialize} /
 * {@code @JsonSerialize} customization that must be rejected on presence-aware members.
 */
final class WrapperCustomization {

  private WrapperCustomization() {}

  /**
   * Checks serialization customization on the property's serialization-side member and
   * deserialization customization on the union of its serialization- and deserialization-side
   * members. Jackson may surface a property-scoped deserialization annotation through the accessor
   * (getter / field), the mutator (creator parameter / setter / field), or both depending on the
   * class shape, so both are inspected to avoid missing setter-, creator-, field-, or getter-placed
   * customization.
   */
  static boolean has(
      JsonMapper mapper,
      @Nullable AnnotatedMember serializationMember,
      @Nullable AnnotatedMember deserializationMember) {
    return hasSerialization(mapper, serializationMember)
        || hasDeserialization(mapper, serializationMember)
        || hasDeserialization(mapper, deserializationMember);
  }

  /**
   * True when the member carries any deserialization customization that would win over plain
   * binding.
   */
  static boolean hasDeserialization(JsonMapper mapper, @Nullable AnnotatedMember accessor) {
    if (accessor == null) {
      return false;
    }
    DeserializationConfig config = mapper.deserializationConfig();
    var introspector = config.getAnnotationIntrospector();
    JavaType declaredType = accessor.getType();
    return introspector.findDeserializer(config, accessor) != null
        || introspector.findKeyDeserializer(config, accessor) != null
        || introspector.findContentDeserializer(config, accessor) != null
        || introspector.findDeserializationConverter(config, accessor) != null
        || introspector.findDeserializationContentConverter(config, accessor) != null
        || typeRefined(
            introspector.refineDeserializationType(config, accessor, declaredType), declaredType);
  }

  private static boolean hasSerialization(JsonMapper mapper, @Nullable AnnotatedMember accessor) {
    if (accessor == null) {
      return false;
    }
    SerializationConfig config = mapper.serializationConfig();
    var introspector = config.getAnnotationIntrospector();
    JavaType declaredType = accessor.getType();
    return introspector.findSerializer(config, accessor) != null
        || introspector.findKeySerializer(config, accessor) != null
        || introspector.findContentSerializer(config, accessor) != null
        || introspector.findNullSerializer(config, accessor) != null
        || introspector.findSerializationConverter(config, accessor) != null
        || introspector.findSerializationContentConverter(config, accessor) != null
        || introspector.findSerializationTyping(config, accessor) != null
        || typeRefined(
            introspector.refineSerializationType(config, accessor, declaredType), declaredType);
  }

  private static boolean typeRefined(@Nullable JavaType refined, JavaType declared) {
    return refined != null && !refined.equals(declared);
  }
}
