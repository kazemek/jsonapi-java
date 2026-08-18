package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Locale;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * Test deserializer that uppercases a string. Used on the low-level path to prove nested atomic
 * conversion preserves property-scoped {@code @JsonDeserialize} authority (ADR-014), and on a
 * presence-aware PATCH shape to mark a member as wrapper-customized and thus invalid on the typed
 * path.
 */
public final class UpperCaseStringDeserializer extends StdDeserializer<String> {

  public UpperCaseStringDeserializer() {
    super(String.class);
  }

  @Override
  public String deserialize(JsonParser parser, DeserializationContext context) {
    return parser.getValueAsString().toUpperCase(Locale.ROOT);
  }
}
