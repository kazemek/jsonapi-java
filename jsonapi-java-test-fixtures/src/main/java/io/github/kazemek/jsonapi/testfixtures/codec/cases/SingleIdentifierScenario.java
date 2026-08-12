package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;

public final class SingleIdentifierScenario {

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
