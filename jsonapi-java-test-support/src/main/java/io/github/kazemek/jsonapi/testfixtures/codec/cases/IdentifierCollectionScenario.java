package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.List;

public final class IdentifierCollectionScenario {

  private IdentifierCollectionScenario() {}

  public static CodecScenario scenario() {
    return CodecScenario.of(
        "identifier-collection",
        "Identifier collection primary data",
        "documents/identifier-collection.json",
        JsonApiDocument.withData(
            new DocumentData.IdentifierCollection(
                List.of(Models.identifier("articles", "1"), Models.identifier("articles", "2")))),
        PrimaryDataKind.RESOURCE_IDENTIFIER,
        SchemaKind.RESPONSE);
  }
}
