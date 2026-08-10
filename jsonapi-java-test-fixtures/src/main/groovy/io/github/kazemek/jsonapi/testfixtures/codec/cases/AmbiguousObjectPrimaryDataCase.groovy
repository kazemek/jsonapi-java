package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataCase

final class AmbiguousObjectPrimaryDataCase {
  private AmbiguousObjectPrimaryDataCase() {}

  static AmbiguousPrimaryDataCase fixture() {
    return AmbiguousPrimaryDataCase.of(
        id: 'ambiguous-object-primary-data',
        notes: 'Object primary data decoding to either a resource or an identifier model',
        expectedPath: 'documents/ambiguous-object-primary-data.json',
        resourceDocument: JsonApiDocument.withData(
        new DocumentData.SingleResource(ResourceObject.of('articles', '1'))),
        identifierDocument: JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of('articles', '1'))))
  }
}
