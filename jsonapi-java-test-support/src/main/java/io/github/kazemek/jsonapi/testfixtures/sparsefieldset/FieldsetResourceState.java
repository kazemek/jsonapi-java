package io.github.kazemek.jsonapi.testfixtures.sparsefieldset;

import io.github.kazemek.jsonapi.core.model.RelationshipData;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Expected mapped resource state: identity, ordered surviving attribute and relationship names, and
 * optional pinned values and linkage.
 *
 * <p>{@code attributeNames == null} means the {@code attributes} member is absent; {@code
 * relationshipNames == null} means the {@code relationships} member is absent. Attribute values and
 * relationship linkage maps may be a subset of the surviving names.
 */
public record FieldsetResourceState(
    String type,
    @Nullable String id,
    @Nullable List<String> attributeNames,
    Map<String, Object> attributeValues,
    @Nullable List<String> relationshipNames,
    Map<String, RelationshipData> relationshipLinkage) {

  public FieldsetResourceState {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(attributeValues, "attributeValues");
    Objects.requireNonNull(relationshipLinkage, "relationshipLinkage");
    if (attributeNames != null) {
      attributeNames = List.copyOf(attributeNames);
    }
    if (relationshipNames != null) {
      relationshipNames = List.copyOf(relationshipNames);
    }
    attributeValues = Map.copyOf(attributeValues);
    relationshipLinkage = Map.copyOf(relationshipLinkage);
  }

  static FieldsetResourceState identity(String type, String id) {
    return new FieldsetResourceState(type, id, null, Map.of(), null, Map.of());
  }

  static FieldsetResourceState of(
      String type,
      String id,
      @Nullable List<String> attributeNames,
      Map<String, Object> attributeValues,
      @Nullable List<String> relationshipNames,
      Map<String, RelationshipData> relationshipLinkage) {
    return new FieldsetResourceState(
        type, id, attributeNames, attributeValues, relationshipNames, relationshipLinkage);
  }
}
