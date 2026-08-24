package io.github.kazemek.jsonapi.testsupport.domainwrite;

import java.util.Map;

/**
 * Comparison policy for a scenario's expected relationship values, consumed by adapter test
 * comparators.
 *
 * <p>Relationship comparison is {@link ComparisonOrder#ORDERED} by default; the Set-based {@code
 * tags} scenario opts into {@link ComparisonOrder#UNORDERED_IDENTIFIER_PAIRS}.
 */
public record DomainWriteComparisonPolicy(Map<String, ComparisonOrder> relationshipOrder) {

  public enum ComparisonOrder {
    ORDERED,
    UNORDERED_IDENTIFIER_PAIRS
  }

  public DomainWriteComparisonPolicy {
    relationshipOrder = Map.copyOf(relationshipOrder);
  }

  public static DomainWriteComparisonPolicy ordered() {
    return new DomainWriteComparisonPolicy(Map.of());
  }

  public ComparisonOrder orderFor(String relationshipName) {
    return relationshipOrder.getOrDefault(relationshipName, ComparisonOrder.ORDERED);
  }
}
