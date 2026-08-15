package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import java.util.List;

final class EmptyErrorsScenario {

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
