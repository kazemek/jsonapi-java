package io.github.kazemek.jsonapi.testfixtures.codec.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.testfixtures.codec.Models
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaDisagreement

final class StringAndObjectLinksCase {
  private StringAndObjectLinksCase() {}

  static CodecFixture fixture() {
    def selfHref = 'http://example.com/articles/1'
    def article = Models.resource(
        'articles',
        '1',
        links: Models.links(self: Models.stringLink(selfHref)))

    def related = Models.objectLink(
        'http://example.com/articles/1/related',
        rel: 'related',
        title: 'Related',
        type: 'application/vnd.api+json',
        hreflang: ['en'],
        meta: Meta.of(count: 1))

    def topLinks = Models.links(
        self: Models.stringLink(selfHref),
        related: related,
        next: null)

    return CodecFixture.of(
        primaryDataKind: PrimaryDataKind.RESOURCE,
        schemaDisagreement: SchemaDisagreement.of(
        reason: 'hreflang canonical list form; draft linkObject.hreflang only accepts a string',
        expected: [
          [keyword: 'type', path: '/links/related/hreflang'],
        ]),
        schemaKind: SchemaKind.RESPONSE,
        id: 'string-and-object-links',
        notes: 'String link, object link, null link, canonical hreflang array',
        expectedPath: 'documents/string-and-object-links.json',
        document: new JsonApiDocument(
        new DocumentData.ResourceCollection([article]),
        null,
        null,
        null,
        topLinks,
        null,
        [:]),
        assertHreflangArray: true)
  }
}
