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

final class LocalIdentifierCase {
  private LocalIdentifierCase() {}

  static CodecFixture fixture() {
    def article = Models.resourceWithLid(
        'articles',
        'temp-1',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.withLid('people', 'temp-author')))))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.CREATE,
        id: 'local-identifier',
        notes: 'Resource and linkage with lid',
        expectedPath: 'documents/local-identifier.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        context: Models.createContext())
  }
}
