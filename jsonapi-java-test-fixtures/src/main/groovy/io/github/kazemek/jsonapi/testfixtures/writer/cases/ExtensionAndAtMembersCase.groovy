package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class ExtensionAndAtMembersCase {
  private ExtensionAndAtMembersCase() {}

  static WriterFixture fixture() {
    def article = Models.resource(
        'articles',
        '1',
        attributes: Attributes.ofAttributes(title: 'Hello'),
        additionalMembers: [
          '@copyright': 'Copyright 2026',
          'ext:version': 1,
        ])
    return WriterFixture.of(
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
