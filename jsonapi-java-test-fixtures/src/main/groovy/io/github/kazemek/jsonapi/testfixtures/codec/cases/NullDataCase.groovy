package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class NullDataCase {
  private NullDataCase() {}

  static CodecFixture fixture() {
    return CodecFixture.of(
        schemaKind: SchemaKind.RESPONSE,
        id: 'null-data',
        notes: 'Explicit data null with meta',
        expectedPath: 'documents/null-data.json',
        document: new JsonApiDocument(
        DocumentData.NullData.INSTANCE,
        null,
        Meta.of(reason: 'deleted'),
        null,
        null,
        null,
        [:]))
  }
}
