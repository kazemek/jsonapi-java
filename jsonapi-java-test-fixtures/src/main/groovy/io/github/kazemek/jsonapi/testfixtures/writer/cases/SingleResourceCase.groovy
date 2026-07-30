package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class SingleResourceCase {
  private SingleResourceCase() {}

  static WriterFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        attributes: Attributes.ofAttributes(title: 'JSON:API paints my bikeshed!'))
    return WriterFixture.of(
        id: 'single-resource',
        notes: 'Single resource primary data',
        expectedPath: 'documents/single-resource.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
