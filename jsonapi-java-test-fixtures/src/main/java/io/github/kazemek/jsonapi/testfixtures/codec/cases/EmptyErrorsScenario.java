package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.List;

public final class EmptyErrorsScenario {

  private EmptyErrorsScenario() {}

  public static CodecScenario scenario() {
    return CodecScenario.of(
        "empty-errors",
        "Present-empty errors array",
        "documents/empty-errors.json",
        JsonApiDocument.withErrors(List.of()),
        null,
        SchemaKind.RESPONSE);
  }
}
