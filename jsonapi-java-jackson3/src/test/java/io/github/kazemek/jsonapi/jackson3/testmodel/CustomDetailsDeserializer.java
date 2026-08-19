package io.github.kazemek.jsonapi.jackson3.testmodel;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Property-scoped {@code @JsonDeserialize} for {@link Details} that returns a known value
 * regardless of the wire content, proving a customized bean-valued nested member is honored
 * atomically rather than recursed into a {@code StructuredPatch} on the low-level path (ADR-014).
 */
public final class CustomDetailsDeserializer extends StdDeserializer<Details> {

  public CustomDetailsDeserializer() {
    super(Details.class);
  }

  @Override
  public Details deserialize(JsonParser parser, DeserializationContext context) {
    return new Details("custom");
  }
}
