package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Locale;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

/** Test deserializer used to mark a nested PATCH member as wrapper-customized (ADR-014). */
public final class UpperCaseStringDeserializer extends StdDeserializer<String> {

  public UpperCaseStringDeserializer() {
    super(String.class);
  }

  @Override
  public String deserialize(JsonParser parser, DeserializationContext context) {
    return parser.getValueAsString().toUpperCase(Locale.ROOT);
  }
}
