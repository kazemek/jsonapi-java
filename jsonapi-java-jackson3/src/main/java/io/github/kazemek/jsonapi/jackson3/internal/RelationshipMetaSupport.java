package io.github.kazemek.jsonapi.jackson3.internal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared helpers for whole-meta mapping properties (ADR-015). */
final class RelationshipMetaSupport {

  private RelationshipMetaSupport() {}

  /**
   * Builds a target-relationship-jsonapi-name to relationship-meta property map. The resolver
   * guarantees at most one relationship-meta property per target, so keys are unique.
   */
  static Map<String, MappingProperty> byTarget(List<MappingProperty> relationshipMetaProperties) {
    Map<String, MappingProperty> byTarget = new LinkedHashMap<>();
    for (MappingProperty property : relationshipMetaProperties) {
      byTarget.put(property.jsonapiName(), property);
    }
    return byTarget;
  }
}
