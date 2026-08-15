package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import java.util.List;

final class EmptyIdentifierCollectionScenario {

  private EmptyIdentifierCollectionScenario() {}

  public static CodecScenario scenario() {
    return CodecScenario.of(
        "empty-identifier-collection",
        "Empty primary data array",
        "documents/empty-identifier-collection.json",
        JsonApiDocument.withData(new DocumentData.IdentifierCollection(List.of())),
        PrimaryDataKind.RESOURCE_IDENTIFIER,
        SchemaKind.RESPONSE);
  }
}
