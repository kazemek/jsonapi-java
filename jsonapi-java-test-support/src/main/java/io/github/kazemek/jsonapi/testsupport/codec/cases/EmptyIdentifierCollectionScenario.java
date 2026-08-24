package io.github.kazemek.jsonapi.testsupport.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testsupport.codec.CodecScenario;
import io.github.kazemek.jsonapi.testsupport.codec.SchemaKind;
import java.util.List;

public final class EmptyIdentifierCollectionScenario {

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
