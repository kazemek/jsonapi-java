package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.JsonApiMembers;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Shared helpers for whole-meta mapping properties and diagnostic pointers (ADR-015). */
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

  /** Resource-relative diagnostic pointer for the resource-side {@code meta} location. */
  static String resourceMetaPath() {
    return "/" + JsonApiMembers.META;
  }

  /**
   * Resource-relative diagnostic pointer for a specific relationship's {@code meta} location, keyed
   * by the relationship's resolved JSON:API member name.
   */
  static String relationshipMetaPath(String relationshipName) {
    return "/" + JsonApiMembers.RELATIONSHIPS + "/" + relationshipName + "/" + JsonApiMembers.META;
  }

  /** Resource-relative diagnostic pointer for the {@code relationships} location. */
  static String relationshipsPath() {
    return "/" + JsonApiMembers.RELATIONSHIPS;
  }
}
