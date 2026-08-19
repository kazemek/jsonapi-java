package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Marker {@code @JsonSerialize} serializer for a presence-aware member, used to prove a
 * non-getter-side (setter) wrapper-level {@code @JsonSerialize} is detected and rejected on typed
 * shape entry (ADR-014). Never actually invoked: the member is rejected during declaration
 * validation.
 */
public final class ConstantPatchPresenceSerializer extends ValueSerializer<PatchPresence<String>> {

  @Override
  public void serialize(
      PatchPresence<String> value, JsonGenerator gen, SerializationContext context)
      throws JacksonException {
    gen.writeString("custom");
  }
}
