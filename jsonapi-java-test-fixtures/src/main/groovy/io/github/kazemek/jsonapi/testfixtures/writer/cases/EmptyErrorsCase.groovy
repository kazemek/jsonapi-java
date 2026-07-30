package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class EmptyErrorsCase {
  private EmptyErrorsCase() {}

  static WriterFixture fixture() {
    return WriterFixture.of(
        id: 'empty-errors',
        notes: 'Present-empty errors array',
        expectedPath: 'documents/empty-errors.json',
        document: JsonApiDocument.withErrors([]))
  }
}
