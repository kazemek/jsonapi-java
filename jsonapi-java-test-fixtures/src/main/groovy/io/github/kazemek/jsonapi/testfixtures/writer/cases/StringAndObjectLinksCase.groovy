package io.github.kazemek.jsonapi.testfixtures.writer.cases

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.testfixtures.writer.Models
import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixture

final class StringAndObjectLinksCase {
  private StringAndObjectLinksCase() {}

  static WriterFixture fixture() {
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

    return WriterFixture.of(
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
