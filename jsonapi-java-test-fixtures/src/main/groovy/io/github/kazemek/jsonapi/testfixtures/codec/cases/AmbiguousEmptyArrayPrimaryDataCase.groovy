package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataCase

final class AmbiguousEmptyArrayPrimaryDataCase {
  private AmbiguousEmptyArrayPrimaryDataCase() {}

  static AmbiguousPrimaryDataCase fixture() {
    return AmbiguousPrimaryDataCase.of(
        id: 'ambiguous-empty-array-primary-data',
        notes: 'Empty-array primary data decoding to either a resource or an identifier model',
        expectedPath: 'documents/ambiguous-empty-array-primary-data.json',
        resourceDocument: JsonApiDocument.withData(new DocumentData.ResourceCollection([])),
        identifierDocument: JsonApiDocument.withData(new DocumentData.IdentifierCollection([])))
  }
}
