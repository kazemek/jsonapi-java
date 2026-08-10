package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class IdentifierCollectionCase {
  private IdentifierCollectionCase() {}

  static CodecFixture fixture() {
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE_IDENTIFIER,
        schemaKind: SchemaKind.RESPONSE,
        id: 'identifier-collection',
        notes: 'Identifier collection primary data',
        expectedPath: 'documents/identifier-collection.json',
        document: JsonApiDocument.withData(new DocumentData.IdentifierCollection([
          Models.identifier('articles', '1'),
          Models.identifier('articles', '2'),
        ])))
  }
}
