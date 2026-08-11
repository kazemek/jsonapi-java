package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class EmptyErrorsCase {
  private EmptyErrorsCase() {}

  static CodecFixture fixture() {
    return CodecFixture.of(
        schemaKind: SchemaKind.RESPONSE,
        id: 'empty-errors',
        notes: 'Present-empty errors array',
        expectedPath: 'documents/empty-errors.json',
        document: JsonApiDocument.withErrors([]))
  }
}
