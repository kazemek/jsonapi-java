package io.github.kazemek.jsonapi.testfixtures.codec

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.Link
import io.github.kazemek.jsonapi.core.model.Links
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.LinksContext
import io.github.kazemek.jsonapi.core.validation.ValidationContext

/** Readable construction helpers for codec fixture cases. */
final class Models {

  private Models() {}

  /** Groovy named-arg form: {@code Models.resource('articles', '1', attributes: ...)}. */
  static ResourceObject resource(Map options = [:], String type, String id) {
    return new ResourceObject(
        type,
        id,
        options.lid as String,
        options.attributes as Attributes,
        options.relationships as Relationships,
        options.links as Links,
        options.meta as Meta,
        (options.additionalMembers ?: [:]) as Map)
  }

  /** Groovy named-arg form: {@code Models.resourceWithLid('articles', 'temp-1', relationships: ...)}. */
  static ResourceObject resourceWithLid(Map options = [:], String type, String lid) {
    return new ResourceObject(
        type,
        null,
        lid,
        options.attributes as Attributes,
        options.relationships as Relationships,
        options.links as Links,
        options.meta as Meta,
        (options.additionalMembers ?: [:]) as Map)
  }

  static ResourceIdentifier identifier(String type, String id) {
    return ResourceIdentifier.of(type, id)
  }

  static ResourceIdentifier withLid(String type, String lid) {
    return ResourceIdentifier.withLid(type, lid)
  }

  /** String-form link: {@code Models.stringLink('http://example.com/articles/1')}. */
  static Link.StringLink stringLink(String href) {
    return new Link.StringLink(href)
  }

  /**
   * Object-form link with optional named members:
   * {@code Models.objectLink(href, rel: 'related', title: 'Related', hreflang: ['en'])}.
   */
  static Link.ObjectLink objectLink(Map options = [:], String href) {
    def hreflang = options.hreflang
    if (hreflang instanceof String) {
      hreflang = [hreflang]
    }
    return new Link.ObjectLink(
        href,
        options.rel as String,
        options.describedby as String,
        options.title as String,
        options.type as String,
        hreflang as List,
        options.meta as Meta,
        (options.additionalMembers ?: [:]) as Map)
  }

  /**
   * Links object from a Groovy map. Avoids IntelliJ false positives on
   * {@code Links.ofLinks(self: ...)} named-arg inference.
   */
  static Links links(Map entries) {
    return Links.ofLinks(entries as Map)
  }

  static ValidationContext defaults() {
    return ValidationContext.defaults()
  }

  static ValidationContext extContext() {
    return new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of("ext"),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        [:],
        null)
  }

  static ValidationContext createContext() {
    return new ValidationContext(
        DocumentUsage.CREATE_REQUEST,
        Set.of(),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        [:],
        null)
  }
}
