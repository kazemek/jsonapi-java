package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class MetaOnlyCase {
  private MetaOnlyCase() {}

  static WriterFixture fixture() {
    return WriterFixture.of(
        id: 'meta-only',
        notes: 'Absent data; meta-only document',
        expectedPath: 'documents/meta-only.json',
        document: JsonApiDocument.withMeta(Meta.of(copyright: 'Copyright 2026')))
  }
}
