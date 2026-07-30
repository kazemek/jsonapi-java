package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class RelationshipNullLinkageCase {
  private RelationshipNullLinkageCase() {}

  static WriterFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))
    return WriterFixture.of(
        id: 'relationship-null-linkage',
        notes: 'Explicit null to-one relationship data',
        expectedPath: 'documents/relationship-null-linkage.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
