package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class NullDataCase {
  private NullDataCase() {}

  static WriterFixture fixture() {
    return WriterFixture.of(
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
