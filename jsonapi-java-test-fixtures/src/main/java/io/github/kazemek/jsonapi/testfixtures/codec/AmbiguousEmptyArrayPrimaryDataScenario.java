package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import java.util.List;

final class AmbiguousEmptyArrayPrimaryDataScenario {

  private AmbiguousEmptyArrayPrimaryDataScenario() {}

  public static AmbiguousPrimaryDataScenario scenario() {
    return new AmbiguousPrimaryDataScenario(
        "ambiguous-empty-array-primary-data",
        "Empty-array primary data decoding to either a resource or an identifier model",
        "documents/ambiguous-empty-array-primary-data.json",
        JsonApiDocument.withData(new DocumentData.ResourceCollection(List.of())),
        JsonApiDocument.withData(new DocumentData.IdentifierCollection(List.of())),
        ValidationContext.defaults());
  }
}
