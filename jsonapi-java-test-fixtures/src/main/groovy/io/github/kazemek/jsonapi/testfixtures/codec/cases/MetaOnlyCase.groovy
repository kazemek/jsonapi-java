package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class MetaOnlyCase {
  private MetaOnlyCase() {}

  static CodecFixture fixture() {
    return CodecFixture.of(
        schemaKind: SchemaKind.RESPONSE,
        id: 'meta-only',
        notes: 'Absent data; meta-only document',
        expectedPath: 'documents/meta-only.json',
        document: JsonApiDocument.withMeta(Meta.of(copyright: 'Copyright 2026')))
  }
}
