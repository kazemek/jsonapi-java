package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Expected mapped resource state: identity, ordered surviving attribute and relationship names,
 * optional pinned values and linkage, and resource-level meta.
 *
 * <p>{@code attributeNames == null} means the {@code attributes} member is absent; {@code
 * relationshipNames == null} means the {@code relationships} member is absent. Attribute values and
 * relationship linkage maps may be a subset of the surviving names. {@code meta == null} means
 * resource meta is absent.
 */
public record FieldsetResourceState(
    String type,
    @Nullable String id,
    @Nullable List<String> attributeNames,
    Map<String, Object> attributeValues,
    @Nullable List<String> relationshipNames,
    Map<String, RelationshipData> relationshipLinkage,
    @Nullable Meta meta) {

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
    return new FieldsetResourceState(type, id, null, Map.of(), null, Map.of(), null);
  }

  static FieldsetResourceState of(
      String type,
      String id,
      @Nullable List<String> attributeNames,
      Map<String, Object> attributeValues,
      @Nullable List<String> relationshipNames,
      Map<String, RelationshipData> relationshipLinkage) {
    return of(
        type, id, attributeNames, attributeValues, relationshipNames, relationshipLinkage, null);
  }

  static FieldsetResourceState of(
      String type,
      String id,
      @Nullable List<String> attributeNames,
      Map<String, Object> attributeValues,
      @Nullable List<String> relationshipNames,
      Map<String, RelationshipData> relationshipLinkage,
      @Nullable Meta meta) {
    return new FieldsetResourceState(
        type, id, attributeNames, attributeValues, relationshipNames, relationshipLinkage, meta);
  }

  /**
   * Asserts that {@code actual} matches this expected fieldset resource state.
   *
   * @throws AssertionError when identity, surviving fields, pinned values, linkage, or meta diverge
   */
  public void assertMatches(ResourceObject actual) {
    Objects.requireNonNull(actual, "actual");
    assertEqual("type", type, actual.type());
    assertEqual("id", id, actual.id());
    assertAttributes(actual);
    assertRelationships(actual);
    assertEqual("meta", meta, actual.meta());
  }

  private void assertAttributes(ResourceObject actual) {
    if (attributeNames == null) {
      if (actual.attributes() != null) {
        throw fail("attributes: expected absent but was " + actual.attributes());
      }
      return;
    }
    var attributes = actual.attributes();
    if (attributes == null) {
      throw fail("attributes: expected " + attributeNames + " but was absent");
    }
    assertEqual("attribute names", attributeNames, List.copyOf(attributes.attributes().keySet()));
    for (Map.Entry<String, Object> entry : attributeValues.entrySet()) {
      assertEqual(
          "attribute " + entry.getKey(),
          entry.getValue(),
          attributes.attributes().get(entry.getKey()));
    }
  }

  private void assertRelationships(ResourceObject actual) {
    if (relationshipNames == null) {
      if (actual.relationships() != null) {
        throw fail("relationships: expected absent but was " + actual.relationships());
      }
      return;
    }
    var relationships = actual.relationships();
    if (relationships == null) {
      throw fail("relationships: expected " + relationshipNames + " but was absent");
    }
    assertEqual(
        "relationship names",
        relationshipNames,
        List.copyOf(relationships.relationships().keySet()));
    for (Map.Entry<String, RelationshipData> entry : relationshipLinkage.entrySet()) {
      Relationship relationship = relationships.relationships().get(entry.getKey());
      RelationshipData actualData = relationship == null ? null : relationship.data();
      assertEqual("relationship " + entry.getKey() + " linkage", entry.getValue(), actualData);
    }
  }

  private static void assertEqual(
      String label, @Nullable Object expected, @Nullable Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw fail(label + ": expected " + expected + " but was " + actual);
    }
  }

  private static AssertionError fail(String message) {
    return new AssertionError(message);
  }
}
