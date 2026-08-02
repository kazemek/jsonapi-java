package io.github.kazemek.jsonapi.jackson3.testmodel;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public final class TitleSerializer extends ValueSerializer<FormattedTitle> {

  @Override
  public Class<FormattedTitle> handledType() {
    return FormattedTitle.class;
  }

  @Override
  public void serialize(FormattedTitle value, JsonGenerator gen, SerializationContext context)
      throws JacksonException {
    gen.writeString("[FORMATTED] " + value.text());
  }
}
