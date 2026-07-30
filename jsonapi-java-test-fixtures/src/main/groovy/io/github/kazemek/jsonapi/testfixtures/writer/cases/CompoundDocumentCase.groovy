package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class CompoundDocumentCase {
  private CompoundDocumentCase() {}

  static WriterFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.identifier('people', '9')))))
    def included = Models.resource(
        'people',
        '9',
        attributes: Attributes.ofAttributes(name: 'Dan'))
    return WriterFixture.of(
        id: 'compound-document',
        notes: 'Compound document with included resources',
        expectedPath: 'documents/compound-document.json',
        document: new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        [included],
        [:]))
  }
}
