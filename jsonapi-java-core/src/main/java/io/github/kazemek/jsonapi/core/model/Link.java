package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.internal.SyntaxValidators;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A JSON:API link as a URI reference string or link object.
 *
 * @apiNote {@link StringLink} is the string form (URI reference). {@link ObjectLink} requires
 *     {@code href} and may carry {@code rel}, {@code describedby}, {@code title}, {@code type},
 *     {@code hreflang}, {@code meta}, and additional members. {@code hreflang} is modeled as a
 *     list; codec emission of single vs array forms is deferred to the Jackson module.
 */
public sealed interface Link permits Link.StringLink, Link.ObjectLink {

  record StringLink(String href) implements Link {
    public StringLink {
      requireValidHref(href, "/links");
    }
  }

  record ObjectLink(
      String href,
      String rel,
      String describedby,
      String title,
      String type,
      List<String> hreflang,
      Meta meta,
      Map<String, Object> additionalMembers)
      implements Link {

    private static final Set<String> RESERVED_ADDITIONAL =
        Set.of("href", "rel", "describedby", "title", "type", "hreflang", "meta");

    public ObjectLink {
      requireValidHref(href, "/links");
      if (rel != null && !SyntaxValidators.isValidLinkRelation(rel)) {
        LocalValidation.fail(
            ValidationRuleCode.INVALID_LINK_RELATION,
            "/links/rel",
            "Invalid link relation: " + rel);
      }
      if (describedby != null && !SyntaxValidators.isValidUriReference(describedby)) {
        LocalValidation.fail(
            ValidationRuleCode.INVALID_URI_REFERENCE,
            "/links/describedby",
            "Invalid describedby URI: " + describedby);
      }
      if (type != null && !SyntaxValidators.isValidMediaType(type)) {
        LocalValidation.fail(
            ValidationRuleCode.INVALID_MEDIA_TYPE, "/links/type", "Invalid media type: " + type);
      }
      if (hreflang != null) {
        for (int i = 0; i < hreflang.size(); i++) {
          String tag = hreflang.get(i);
          if (!SyntaxValidators.isValidLanguageTag(tag)) {
            LocalValidation.fail(
                ValidationRuleCode.INVALID_LANGUAGE_TAG,
                "/links/hreflang/" + i,
                "Invalid language tag: " + tag);
          }
        }
        hreflang = List.copyOf(hreflang);
      }
      additionalMembers =
          AdditionalMembers.copy(
              additionalMembers, "/links", "Invalid link member name: ", RESERVED_ADDITIONAL);
    }

    public static ObjectLink ofHref(String href) {
      return new ObjectLink(href, null, null, null, null, null, null, Map.of());
    }

    public static ObjectLink withHreflang(String href, List<String> hreflang) {
      return new ObjectLink(href, null, null, null, null, hreflang, null, Map.of());
    }

    public static ObjectLink withHreflang(String href, String singleLanguage) {
      return new ObjectLink(href, null, null, null, null, List.of(singleLanguage), null, Map.of());
    }
  }

  private static void requireValidHref(String href, String path) {
    if (!SyntaxValidators.isValidUriReference(href)) {
      LocalValidation.fail(
          ValidationRuleCode.INVALID_URI_REFERENCE, path + "/href", "Invalid link href: " + href);
    }
  }
}
