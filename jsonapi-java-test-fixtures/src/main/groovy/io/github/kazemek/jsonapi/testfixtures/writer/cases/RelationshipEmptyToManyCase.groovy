package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class RelationshipEmptyToManyCase {
  private RelationshipEmptyToManyCase() {}

  static WriterFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(
        comments: Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty())))
    return WriterFixture.of(
        id: 'relationship-empty-to-many',
        notes: 'Empty to-many relationship data array',
        expectedPath: 'documents/relationship-empty-to-many.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
