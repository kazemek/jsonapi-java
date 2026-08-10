package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind

final class CompoundDocumentCase {
  private CompoundDocumentCase() {}

  static CodecFixture fixture() {
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
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
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
