package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class EmptyIdentifierCollectionCase {
  private EmptyIdentifierCollectionCase() {}

  static WriterFixture fixture() {
    return WriterFixture.of(
        id: 'empty-identifier-collection',
        notes: 'Empty primary data array',
        expectedPath: 'documents/empty-identifier-collection.json',
        document: JsonApiDocument.withData(new DocumentData.IdentifierCollection([])))
  }
}
