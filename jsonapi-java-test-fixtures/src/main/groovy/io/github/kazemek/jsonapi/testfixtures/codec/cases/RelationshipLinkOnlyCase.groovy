package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class RelationshipLinkOnlyCase {
  private RelationshipLinkOnlyCase() {}

  static CodecFixture fixture() {
    def authorLinks = Models.links(
        self: Models.stringLink('http://example.com/articles/1/relationships/author'),
        related: Models.stringLink('http://example.com/articles/1/author'))
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(author: Relationship.linkOnly(authorLinks)))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'relationship-link-only',
        notes: 'Link-only relationship without data',
        expectedPath: 'documents/relationship-link-only.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
