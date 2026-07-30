package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class LocalIdentifierCase {
  private LocalIdentifierCase() {}

  static WriterFixture fixture() {
    def article = Models.resourceWithLid(
        'articles',
        'temp-1',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.withLid('people', 'temp-author')))))
    return WriterFixture.of(
        id: 'local-identifier',
        notes: 'Resource and linkage with lid',
        expectedPath: 'documents/local-identifier.json',
        document: JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        context: Models.createContext())
  }
}
