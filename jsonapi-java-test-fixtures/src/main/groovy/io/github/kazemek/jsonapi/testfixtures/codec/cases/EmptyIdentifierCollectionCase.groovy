package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class EmptyIdentifierCollectionCase {
  private EmptyIdentifierCollectionCase() {}

  static CodecFixture fixture() {
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE_IDENTIFIER,
        schemaKind: SchemaKind.RESPONSE,
        id: 'empty-identifier-collection',
        notes: 'Empty primary data array',
        expectedPath: 'documents/empty-identifier-collection.json',
        document: JsonApiDocument.withData(new DocumentData.IdentifierCollection([])))
  }
}
