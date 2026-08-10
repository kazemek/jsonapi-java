package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class RelationshipNullLinkageCase {
  private RelationshipNullLinkageCase() {}

  static CodecFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(RelationshipData.NullLinkage.INSTANCE)))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'relationship-null-linkage',
        notes: 'Explicit null to-one relationship data',
        expectedPath: 'documents/relationship-null-linkage.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
