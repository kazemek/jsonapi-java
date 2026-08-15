package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;

final class SingleIdentifierScenario {

  private SingleIdentifierScenario() {}

  public static CodecScenario scenario() {
    return CodecScenario.of(
        "single-identifier",
        "Single resource identifier primary data",
        "documents/single-identifier.json",
        JsonApiDocument.withData(
            new DocumentData.SingleIdentifier(Models.identifier("articles", "1"))),
        PrimaryDataKind.RESOURCE_IDENTIFIER,
        SchemaKind.RESPONSE);
  }
}
