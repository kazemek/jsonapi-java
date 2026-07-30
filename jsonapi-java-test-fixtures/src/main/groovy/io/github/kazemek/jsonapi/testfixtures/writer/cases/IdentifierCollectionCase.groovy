package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class IdentifierCollectionCase {
  private IdentifierCollectionCase() {}

  static WriterFixture fixture() {
    return WriterFixture.of(
        id: 'identifier-collection',
        notes: 'Identifier collection primary data',
        expectedPath: 'documents/identifier-collection.json',
        document: JsonApiDocument.withData(new DocumentData.IdentifierCollection([
          Models.identifier('articles', '1'),
          Models.identifier('articles', '2'),
        ])))
  }
}
