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

final class CompoundNestedIntermediateCase {
  private static final String TYPE_ARTICLES = 'articles'
  private static final String TYPE_COMMENTS = 'comments'
  private static final String TYPE_PEOPLE = 'people'

  private CompoundNestedIntermediateCase() {}

  static CodecFixture fixture() {
    def article = Models.resource(
        TYPE_ARTICLES,
        '1',
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, '9'))),
        comments: Relationship.withData(
        new RelationshipData.IdentifierCollectionLinkage([
          Models.identifier(TYPE_COMMENTS, '5'),
          Models.identifier(TYPE_COMMENTS, '12'),
        ]))))
    def comment5 = Models.resource(
        TYPE_COMMENTS,
        '5',
        attributes: Attributes.ofAttributes(body: 'First!'),
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, '2')))))
    def person2 = Models.resource(
        TYPE_PEOPLE,
        '2',
        attributes: Attributes.ofAttributes(name: 'Ezra'))
    def comment12 = Models.resource(
        TYPE_COMMENTS,
        '12',
        attributes: Attributes.ofAttributes(body: 'I like XML better'),
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, '9')))))
    def person9 = Models.resource(
        TYPE_PEOPLE,
        '9',
        attributes: Attributes.ofAttributes(name: 'Dan'))
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaKind: SchemaKind.RESPONSE,
        id: 'compound-nested-intermediate',
        notes: 'Compound document with nested comments.author intermediates',
        expectedPath: 'documents/compound-nested-intermediate.json',
        document: new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        [
          comment5,
          comment12,
          person2,
          person9
        ],
        [:]))
  }
}
