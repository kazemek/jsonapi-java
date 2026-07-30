package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class EmptyIncludedCase {
  private EmptyIncludedCase() {}

  static WriterFixture fixture() {
    def article = Models.resource('articles', '1')
    return WriterFixture.of(
        id: 'empty-included',
        notes: 'Present-empty included array with primary data',
        expectedPath: 'documents/empty-included.json',
        document: new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        [],
        [:]))
  }
}
