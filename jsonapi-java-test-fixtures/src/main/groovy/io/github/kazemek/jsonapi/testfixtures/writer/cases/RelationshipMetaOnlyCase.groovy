package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class RelationshipMetaOnlyCase {
  private RelationshipMetaOnlyCase() {}

  static WriterFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(
        author: Relationship.metaOnly(Meta.of(inferred: true))))
    return WriterFixture.of(
        id: 'relationship-meta-only',
        notes: 'Meta-only relationship without data',
        expectedPath: 'documents/relationship-meta-only.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
