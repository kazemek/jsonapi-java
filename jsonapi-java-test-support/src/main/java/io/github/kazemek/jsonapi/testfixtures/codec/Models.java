package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.core.validation.DocumentUsage;
import io.github.kazemek.jsonapi.core.validation.LinksContext;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** Readable construction helpers for codec fixture cases. */
public final class Models {

  private Models() {}

  public static ResourceObject resource(String type, String id) {
    return new ResourceObject(type, id, null, null, null, null, null, Map.of());
  }

  public static ResourceObject resource(String type, String id, Attributes attributes) {
    return new ResourceObject(type, id, null, attributes, null, null, null, Map.of());
  }

  public static ResourceObject resource(String type, String id, Relationships relationships) {
    return new ResourceObject(type, id, null, null, relationships, null, null, Map.of());
  }

  public static ResourceObject resource(String type, String id, Links links) {
    return new ResourceObject(type, id, null, null, null, links, null, Map.of());
  }

  public static ResourceObject resource(
      String type, String id, Attributes attributes, Relationships relationships) {
    return new ResourceObject(type, id, null, attributes, relationships, null, null, Map.of());
  }

  public static ResourceObject resource(
      String type, String id, Attributes attributes, Map<String, Object> additionalMembers) {
    return new ResourceObject(type, id, null, attributes, null, null, null, additionalMembers);
  }

  public static ResourceObject resource(
      String type,
      String id,
      Attributes attributes,
      Relationships relationships,
      Links links,
      Meta meta) {
    return new ResourceObject(type, id, null, attributes, relationships, links, meta, Map.of());
  }

  public static ResourceObject resourceWithLid(
      String type, String lid, Relationships relationships) {
    return new ResourceObject(type, null, lid, null, relationships, null, null, Map.of());
  }

  public static ResourceIdentifier identifier(String type, String id) {
    return ResourceIdentifier.of(type, id);
  }

  public static ResourceIdentifier withLid(String type, String lid) {
    return ResourceIdentifier.withLid(type, lid);
  }

  public static Link.StringLink stringLink(String href) {
    return new Link.StringLink(href);
  }

  public static Link.ObjectLink objectLink(
      String href, String rel, String title, String type, List<String> hreflang, Meta meta) {
    return new Link.ObjectLink(href, rel, null, title, type, hreflang, meta, Map.of());
  }

  public static Links links(Map<String, @Nullable Link> entries) {
    return Links.ofLinks(entries);
  }

  public static ValidationContext extContext() {
    return new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of("ext"),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of(),
        null);
  }

  public static ValidationContext createContext() {
    return new ValidationContext(
        DocumentUsage.CREATE_REQUEST,
        Set.of(),
        Set.of(),
        Set.of(),
        false,
        LinksContext.TOP_LEVEL,
        Map.of(),
        null);
  }
}
