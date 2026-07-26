package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.MemberNames;
import io.github.kazemek.jsonapi.core.internal.OpenJsonValues;
import io.github.kazemek.jsonapi.core.internal.OrderedMaps;
import io.github.kazemek.jsonapi.core.validation.LinksContext;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Flat links object with nullable link values and pass-through members. */
public final class Links {

  private static final String PATH = "/links";
  private static final Set<String> TOP_LEVEL_STANDARD =
      Set.of("self", "related", "describedby", "first", "last", "prev", "next");
  private static final Set<String> RESOURCE_STANDARD = Set.of("self");
  private static final Set<String> RELATIONSHIP_STANDARD =
      Set.of("self", "related", "describedby", "first", "last", "prev", "next");
  private static final Set<String> ERROR_STANDARD = Set.of("about", "type");

  private final Map<String, Link> entries;
  private final Map<String, Object> additionalMembers;

  private Links(Map<String, Link> entries, Map<String, Object> additionalMembers) {
    this.entries = entries;
    this.additionalMembers = additionalMembers;
  }

  public static Links empty() {
    return new Links(Map.of(), Map.of());
  }

  public static Links of(Map<String, Link> links, Map<String, ?> additionalMembers) {
    Map<String, Link> linkCopy = OrderedMaps.copyOfNullableValues(links);
    Map<String, Object> additionalCopy = copyAdditionalMembers(additionalMembers);
    OrderedMaps.requireNoCollisions(linkCopy, castLinks(additionalCopy), "links", PATH);
    return new Links(linkCopy, additionalCopy);
  }

  public static Links ofLinks(Map<String, Link> links) {
    return of(links, Map.of());
  }

  public Map<String, Link> links() {
    return entries;
  }

  public Map<String, Object> additionalMembers() {
    return additionalMembers;
  }

  public boolean isEmpty() {
    return entries.isEmpty() && additionalMembers.isEmpty();
  }

  public Map<String, Object> flatten() {
    Map<String, Object> flat = new LinkedHashMap<>();
    flat.putAll(entries);
    flat.putAll(additionalMembers);
    return OrderedMaps.copyOfNullableValues(flat);
  }

  public boolean hasStandardMember(String name, LinksContext context) {
    return standardMembers(context).contains(name);
  }

  public static Set<String> standardMembers(LinksContext context) {
    return switch (context) {
      case TOP_LEVEL -> TOP_LEVEL_STANDARD;
      case RESOURCE -> RESOURCE_STANDARD;
      case RELATIONSHIP -> RELATIONSHIP_STANDARD;
      case ERROR -> ERROR_STANDARD;
    };
  }

  private static Map<String, Object> copyAdditionalMembers(Map<String, ?> source) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> copy = new LinkedHashMap<>();
    for (Map.Entry<String, ?> entry : source.entrySet()) {
      String name = entry.getKey();
      if (!MemberNames.isValid(name)) {
        LocalValidation.fail(
            ValidationRuleCode.INVALID_MEMBER_NAME,
            PATH + "/" + name,
            "Invalid links member name: " + name);
      }
      copy.put(name, OpenJsonValues.copy(entry.getValue(), PATH + "/" + name));
    }
    return OrderedMaps.copyOfNullableValues(copy);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Link> castLinks(Map<String, Object> map) {
    return (Map) map;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Links that)) {
      return false;
    }
    return entries.equals(that.entries) && additionalMembers.equals(that.additionalMembers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entries, additionalMembers);
  }
}
