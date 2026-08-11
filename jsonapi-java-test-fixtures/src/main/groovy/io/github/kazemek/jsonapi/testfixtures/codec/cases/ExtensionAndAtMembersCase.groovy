package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaDisagreement

final class ExtensionAndAtMembersCase {
  private ExtensionAndAtMembersCase() {}

  static CodecFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        attributes: Attributes.ofAttributes(title: 'Hello'),
        additionalMembers: [
          '@copyright': 'Copyright 2026',
          'ext:version': 1,
        ])
    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaDisagreement: SchemaDisagreement.of(
        reason: 'top-level ext: member; PR json-api/json-api#1603 does not yet model extension members (see its description)',
        expected: [
          [keyword: 'unevaluatedProperties', path: ''],
        ]),
        schemaKind: SchemaKind.RESPONSE,
        id: 'extension-and-at-members',
        notes: 'Extension and @ members on document and resource',
        expectedPath: 'documents/extension-and-at-members.json',
        document: new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null,
        null,
        null,
        null,
        null,
        ['ext:request-id': 'abc-123']),
        context: Models.extContext())
  }
}
