package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.ErrorObject
import io.github.kazemek.jsonapi.core.model.ErrorSource
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class ErrorsDocumentCase {
  private ErrorsDocumentCase() {}

  static CodecFixture fixture() {
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
    return CodecFixture.of(
        schemaKind: SchemaKind.RESPONSE,
        id: 'errors-document',
        notes: 'Top-level errors with source and links',
        expectedPath: 'documents/errors-document.json',
        document: JsonApiDocument.withErrors([error]))
  }
}
