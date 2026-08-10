package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.JsonApiObject
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaDisagreement

final class MemberOrderCase {
  private MemberOrderCase() {}

  static CodecFixture fixture() {
    def self = Models.stringLink('http://example.com/articles/1')
    def article = Models.resource(
        'articles',
        '1',
        lid: 'temp-1',
        attributes: Attributes.ofAttributes(title: 'Ordered'),
        relationships: Relationships.ofRelationships(
        author: Relationship.withData(
        new RelationshipData.SingleLinkage(Models.identifier('people', '9')))),
        links: Models.links(self: self),
        meta: Meta.of(created: '2026-01-01'),
        additionalMembers: ['ext:flag': true])
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaDisagreement: SchemaDisagreement.of(
        reason: 'response resource carries both id and lid and top-level ext: members; the draft schema requires id and forbids lid in response resources and only models @ members',
        expected: [
          [keyword: 'not', path: '/data'],
          [keyword: 'unevaluatedProperties', path: ''],
        ]),
        schemaKind: SchemaKind.RESPONSE,
        id: 'member-order',
        notes: 'Canonical standard member order then additional members',
        expectedPath: 'documents/member-order.json',
        document: new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        Meta.of(copyright: 'Copyright 2026'),
        JsonApiObject.ofVersion('1.1'),
        Models.links(self: self),
        [
          ResourceObject.of('people', '9')
        ],
        ['ext:trace': 't-1']),
        context: Models.extContext(),
        assertExactUtf8: true,
        exactUtf8Path: 'documents/member-order.compact.json')
  }
}
