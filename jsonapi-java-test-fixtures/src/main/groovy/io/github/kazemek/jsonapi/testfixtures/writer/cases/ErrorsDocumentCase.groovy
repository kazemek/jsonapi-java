package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.ErrorObject
import io.github.kazemek.jsonapi.core.model.ErrorSource
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class ErrorsDocumentCase {
  private ErrorsDocumentCase() {}

  static WriterFixture fixture() {
    def error = new ErrorObject(
        '1',
        Models.links(about: Models.stringLink('http://example.com/docs/errors/invalid')),
        '422',
        'invalid',
        'Invalid Attribute',
        'Title is required',
        new ErrorSource('/data/attributes/title', null, null, [:]),
        null,
        [:])
    return WriterFixture.of(
        id: 'errors-document',
        notes: 'Top-level errors with source and links',
        expectedPath: 'documents/errors-document.json',
        document: JsonApiDocument.withErrors([error]))
  }
}
