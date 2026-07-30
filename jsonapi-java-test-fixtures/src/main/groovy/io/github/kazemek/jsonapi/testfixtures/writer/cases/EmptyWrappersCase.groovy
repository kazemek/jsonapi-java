package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class EmptyWrappersCase {
  private EmptyWrappersCase() {}

  static WriterFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        attributes: Attributes.empty(),
        relationships: Relationships.empty(),
        links: Links.empty(),
        meta: Meta.empty())
    return WriterFixture.of(
        id: 'empty-wrappers',
        notes: 'Present-empty attributes, relationships, links, meta',
        expectedPath: 'documents/empty-wrappers.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
