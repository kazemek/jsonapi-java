package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataCase;
import java.util.List;

public final class AmbiguousEmptyArrayPrimaryDataCase {

  private AmbiguousEmptyArrayPrimaryDataCase() {}

  public static AmbiguousPrimaryDataCase fixture() {
    return new AmbiguousPrimaryDataCase(
        "ambiguous-empty-array-primary-data",
        "Empty-array primary data decoding to either a resource or an identifier model",
        "documents/ambiguous-empty-array-primary-data.json",
        JsonApiDocument.withData(new DocumentData.ResourceCollection(List.of())),
        JsonApiDocument.withData(new DocumentData.IdentifierCollection(List.of())),
        ValidationContext.defaults());
  }
}
