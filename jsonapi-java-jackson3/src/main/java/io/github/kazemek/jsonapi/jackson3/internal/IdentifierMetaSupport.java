package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.JsonApiMembers;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.MappingLocation;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Shared helpers for identifier-meta mapping properties, diagnostic locations, and linkage copies
 * that preserve {@link ResourceIdentifier#meta()} (ADR-017).
 */
final class IdentifierMetaSupport {

  private IdentifierMetaSupport() {}

  /**
   * Builds a target-relationship-jsonapi-name to identifier-meta property map. The resolver
   * guarantees at most one identifier-meta property per target, so keys are unique.
   */
  static Map<String, MappingProperty> byTarget(List<MappingProperty> identifierMetaProperties) {
    return PatchMemberConverter.byJsonapiName(identifierMetaProperties);
  }

  /**
   * Resource-relative diagnostic location for to-one identifier meta, or for declaration failures
   * that address the identifier-meta property as a whole.
   */
  static MappingLocation identifierMetaLocation(String relationshipName) {
    return MappingLocation.of(
        JsonApiMembers.RELATIONSHIPS, relationshipName, JsonApiMembers.DATA, JsonApiMembers.META);
  }

  /**
   * Resource-relative diagnostic location for one to-many linkage identifier's {@code meta} member.
   */
  static MappingLocation identifierMetaLocation(String relationshipName, int index) {
    return MappingLocation.of(
        JsonApiMembers.RELATIONSHIPS,
        relationshipName,
        JsonApiMembers.DATA,
        Integer.toString(index),
        JsonApiMembers.META);
  }

  /**
   * Copies linkage identity and identifier meta, dropping additional members (existing built-in
   * conversion scope).
   */
  static ResourceIdentifier copyLinkageIdentifier(ResourceIdentifier identifier) {
    return new ResourceIdentifier(
        identifier.type(), identifier.id(), identifier.lid(), identifier.meta(), Map.of());
  }

  static ResourceIdentifier withMeta(ResourceIdentifier identifier, @Nullable Meta meta) {
    return new ResourceIdentifier(
        identifier.type(), identifier.id(), identifier.lid(), meta, Map.of());
  }
}
