package io.github.kazemek.jsonapi.testfixtures.sparsefieldset;

import java.util.Objects;
import java.util.Set;

/**
 * Shared pre-access filtering guarantee: excluded attribute getters and off-path relationship
 * getters remain unread. Exact single-read counts stay adapter-suite assertions (ADR-004).
 */
public record ZeroReadGuarantee(Set<String> unreadAttributes, Set<String> unreadRelationships) {

  public ZeroReadGuarantee {
    Objects.requireNonNull(unreadAttributes, "unreadAttributes");
    Objects.requireNonNull(unreadRelationships, "unreadRelationships");
    unreadAttributes = Set.copyOf(unreadAttributes);
    unreadRelationships = Set.copyOf(unreadRelationships);
  }
}
