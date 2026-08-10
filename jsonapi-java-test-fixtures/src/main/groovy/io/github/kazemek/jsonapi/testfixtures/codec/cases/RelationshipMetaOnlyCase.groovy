package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class RelationshipMetaOnlyCase {
  private RelationshipMetaOnlyCase() {}

  static CodecFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(
        author: Relationship.metaOnly(Meta.of(inferred: true))))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'relationship-meta-only',
        notes: 'Meta-only relationship without data',
        expectedPath: 'documents/relationship-meta-only.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)))
  }
}
