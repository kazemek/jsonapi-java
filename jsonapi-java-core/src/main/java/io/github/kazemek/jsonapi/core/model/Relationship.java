package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.internal.MemberNames;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.Map;
import java.util.Set;

/** A JSON:API relationship with optional data, links, meta, and additional members. */
public record Relationship(
    RelationshipData data, Links links, Meta meta, Map<String, Object> additionalMembers) {

  private static final Set<String> PAGINATION_LINKS = Set.of("first", "last", "prev", "next");

  private static final Set<String> RESERVED_ADDITIONAL = Set.of("data", "links", "meta");

  public Relationship {
    additionalMembers =
        AdditionalMembers.copy(
            additionalMembers,
            "/relationships",
            "Invalid relationship member name: ",
            RESERVED_ADDITIONAL);
    boolean hasExtension = hasExtensionMembers(additionalMembers);
    boolean hasMember = data != null || links != null || meta != null || hasExtension;
    if (!hasMember) {
      LocalValidation.fail(
          ValidationRuleCode.MISSING_RELATIONSHIP_MEMBER,
          "/relationships",
          "Relationship must contain at least one of data, links, meta, or extension members");
    }
    if (data == null && meta == null && !hasExtension) {
      if (!hasNonPaginationRelationshipLink(links)) {
        LocalValidation.fail(
            ValidationRuleCode.MISSING_RELATIONSHIP_MEMBER,
            "/relationships",
            "Links-only relationship must contain a non-pagination link"
                + " (self, related, extension, or profile relation)");
      }
    }
  }

  public static Relationship linkOnly(Links links) {
    return new Relationship(null, links, null, Map.of());
  }

  public static Relationship metaOnly(Meta meta) {
    return new Relationship(null, null, meta, Map.of());
  }

  public static Relationship withData(RelationshipData data) {
    return new Relationship(data, null, null, Map.of());
  }

  public boolean hasDataMember() {
    return data != null;
  }

  private static boolean hasExtensionMembers(Map<String, ?> members) {
    if (members == null || members.isEmpty()) {
      return false;
    }
    for (String name : members.keySet()) {
      if (MemberNames.isExtensionMember(name)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasNonPaginationRelationshipLink(Links links) {
    for (String name : links.links().keySet()) {
      if (!PAGINATION_LINKS.contains(name)) {
        return true;
      }
    }
    return false;
  }
}
