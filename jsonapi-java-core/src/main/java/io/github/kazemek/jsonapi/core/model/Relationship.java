package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.internal.MemberNames;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.Map;

/** A JSON:API relationship with optional data, links, meta, and additional members. */
public record Relationship(
    RelationshipData data, Links links, Meta meta, Map<String, Object> additionalMembers) {

  public Relationship {
    additionalMembers =
        AdditionalMembers.copy(
            additionalMembers, "/relationships", "Invalid relationship member name: ");
    boolean hasMember =
        data != null || links != null || meta != null || hasExtensionMembers(additionalMembers);
    if (!hasMember) {
      LocalValidation.fail(
          ValidationRuleCode.MISSING_RELATIONSHIP_MEMBER,
          "/relationships",
          "Relationship must contain at least one of data, links, meta, or extension members");
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
}
