package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class RelationshipLinkOnlyCase {
  private RelationshipLinkOnlyCase() {}

  static WriterFixture fixture() {
    def authorLinks = Models.links(
        self: Models.stringLink('http://example.com/articles/1/relationships/author'),
        related: Models.stringLink('http://example.com/articles/1/author'))
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(author: Relationship.linkOnly(authorLinks)))
    return WriterFixture.of(
        id: 'relationship-link-only',
        notes: 'Link-only relationship without data',
        expectedPath: 'documents/relationship-link-only.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
