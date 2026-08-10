package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class SingleResourceCase {
  private SingleResourceCase() {}

  static CodecFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        attributes: Attributes.ofAttributes(title: 'JSON:API paints my bikeshed!'))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'single-resource',
        notes: 'Single resource primary data',
        expectedPath: 'documents/single-resource.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
