package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class SingleIdentifierCase {
  private SingleIdentifierCase() {}

  static WriterFixture fixture() {
    return WriterFixture.of(
        id: 'single-identifier',
        notes: 'Single resource identifier primary data',
        expectedPath: 'documents/single-identifier.json',
        document: JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(Models.identifier('articles', '1'))))
  }
}
