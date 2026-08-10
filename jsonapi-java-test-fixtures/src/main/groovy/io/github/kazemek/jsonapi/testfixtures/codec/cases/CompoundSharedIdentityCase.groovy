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

final class CompoundSharedIdentityCase {
  private static final String TYPE_ARTICLES = 'articles'
  private static final String TYPE_PEOPLE = 'people'

  private CompoundSharedIdentityCase() {}

  static CodecFixture fixture() {
    def article1 = Models.resource(
        TYPE_ARTICLES,
        '1',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, '9')))))
    def article2 = Models.resource(
        TYPE_ARTICLES,
        '2',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, '9')))))
    def included = Models.resource(
        TYPE_PEOPLE,
        '9',
        attributes: Attributes.ofAttributes(name: 'Dan'))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'compound-shared-identity',
        notes: 'Compound collection sharing one included author identity',
        expectedPath: 'documents/compound-shared-identity.json',
        document: new JsonApiDocument(
        new DocumentData.ResourceCollection([article1, article2]),
        null,
        null,
        null,
        null,
        [included],
        [:]))
  }
}
