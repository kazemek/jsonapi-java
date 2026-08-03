package io.github.kazemek.jsonapi.jackson3;

import java.util.Objects;
import java.util.Set;

/**
 * Decides which relationships may be traversed for compound-document inclusion.
 *
 * <p>Policy governs inclusion traversal only. Linkage emission on selected resources remains
 * unaffected.
 */
public final class IncludePolicy {

  private enum Mode {
    DENY_ALL,
    ALLOW_ALL,
    ALLOWING
  }

  private final Mode mode;
  private final Set<RelationshipAllowance> allowances;

  private IncludePolicy(Mode mode, Set<RelationshipAllowance> allowances) {
    this.mode = mode;
    this.allowances = allowances;
  }

  /** Rejects every relationship for inclusion traversal. */
  public static IncludePolicy denyAll() {
    return new IncludePolicy(Mode.DENY_ALL, Set.of());
  }

  /** Permits every mapped relationship for inclusion traversal. */
  public static IncludePolicy allowAll() {
    return new IncludePolicy(Mode.ALLOW_ALL, Set.of());
  }

  /**
   * Permits only the given owner-type + relationship allowances. The allowance set is defensively
   * copied.
   */
  public static IncludePolicy allowing(Set<RelationshipAllowance> allowances) {
    Objects.requireNonNull(allowances, "allowances");
    return new IncludePolicy(Mode.ALLOWING, Set.copyOf(allowances));
  }

  /**
   * Returns whether inclusion traversal may follow {@code relationshipName} on a resource whose
   * JSON:API type is {@code ownerResourceType}.
   */
  public boolean allows(String ownerResourceType, String relationshipName) {
    Objects.requireNonNull(ownerResourceType, "ownerResourceType");
    Objects.requireNonNull(relationshipName, "relationshipName");
    return switch (mode) {
      case DENY_ALL -> false;
      case ALLOW_ALL -> true;
      case ALLOWING ->
          allowances.contains(RelationshipAllowance.of(ownerResourceType, relationshipName));
    };
  }
}
