package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class SingleIdentifierCase {
  private SingleIdentifierCase() {}

  static CodecFixture fixture() {
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE_IDENTIFIER,
        schemaKind: SchemaKind.RESPONSE,
        id: 'single-identifier',
        notes: 'Single resource identifier primary data',
        expectedPath: 'documents/single-identifier.json',
        document: JsonApiDocument.withData(
        new DocumentData.SingleIdentifier(Models.identifier('articles', '1'))))
  }
}
