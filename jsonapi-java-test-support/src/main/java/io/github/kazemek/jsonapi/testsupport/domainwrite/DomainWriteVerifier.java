package io.github.kazemek.jsonapi.testsupport.domainwrite;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Adapter-neutral semantic comparison for {@link DomainWriteScenario} outcomes. Jackson-major
 * suites invoke their own mapper, then hand the result here so Jackson 2 does not copy resource,
 * relationship, identifier, or document comparison.
 */
public final class DomainWriteVerifier {

  private DomainWriteVerifier() {}

  /**
   * Asserts that {@code result} and {@code thrown} match {@code scenario}'s discriminated outcome.
   *
   * @throws AssertionError when the observed result diverges from the catalog expectation
   */
  public static void verify(
      DomainWriteScenario scenario, @Nullable Object result, @Nullable Throwable thrown) {
    Objects.requireNonNull(scenario, "scenario");
    DomainWriteOutcome outcome = scenario.outcome();
    if (outcome instanceof DomainWriteOutcome.Failure(Class<? extends Throwable> expectedType)) {
      if (thrown == null) {
        throw fail("expected " + expectedType.getName() + forScenario(scenario.id()));
      }
      if (!expectedType.isInstance(thrown)) {
        throw fail(
            "expected "
                + expectedType.getName()
                + forScenario(scenario.id())
                + expectedButWas(expectedType.getName(), thrown.getClass().getName()));
      }
      return;
    }
    if (thrown != null) {
      throw fail("unexpected " + thrown.getClass().getName() + forScenario(scenario.id()), thrown);
    }
    assertSuccess(scenario, (DomainWriteOutcome.Success) outcome, result);
  }

  private static void assertSuccess(
      DomainWriteScenario scenario, DomainWriteOutcome.Success success, @Nullable Object result) {
    DomainWriteOperation operation = scenario.operation();
    DomainWriteComparisonPolicy policy = scenario.comparisonPolicy();
    if (operation == DomainWriteOperation.TO_RESOURCE) {
      if (!(result instanceof ResourceObject actual)) {
        throw fail(
            "expected ResourceObject"
                + forScenario(scenario.id())
                + expectedButWas("ResourceObject", typeName(result)));
      }
      assertResource(Objects.requireNonNull(success.resource(), "resource"), actual, policy);
      return;
    }
    if (operation == DomainWriteOperation.TO_DOCUMENT
        || operation == DomainWriteOperation.TO_DOCUMENT_WITH_ENVELOPE
        || operation == DomainWriteOperation.TO_RESOURCE_COLLECTION) {
      if (!(result instanceof JsonApiDocument actual)) {
        throw fail(
            "expected JsonApiDocument"
                + forScenario(scenario.id())
                + expectedButWas("JsonApiDocument", typeName(result)));
      }
      assertDocument(Objects.requireNonNull(success.document(), "document"), actual, policy);
      return;
    }
    throw fail("unknown operation " + operation + forScenario(scenario.id()));
  }

  static void assertResource(
      ResourceObject expected, ResourceObject actual, DomainWriteComparisonPolicy policy) {
    assertEqual("type", expected.type(), actual.type());
    assertEqual("id", expected.id(), actual.id());
    assertEqual("lid", expected.lid(), actual.lid());
    assertAttributes(expected.attributes(), actual.attributes());
    assertRelationships(expected.relationships(), actual.relationships(), policy);
    assertEqual("links", expected.links(), actual.links());
    assertEqual("meta", expected.meta(), actual.meta());
    assertEqual("additionalMembers", expected.additionalMembers(), actual.additionalMembers());
  }

  static void assertDocument(
      JsonApiDocument expected, JsonApiDocument actual, DomainWriteComparisonPolicy policy) {
    DocumentData expectedData = expected.data();
    if (expectedData == null) {
      throw fail("expected primary data was absent");
    }
    DocumentData actualData = actual.data();
    switch (expectedData) {
      case DocumentData.SingleResource(ResourceObject expectedResource) -> {
        if (!(actualData instanceof DocumentData.SingleResource(ResourceObject actualResource))) {
          throw fail(
              "expected single-resource primary data"
                  + expectedButWas("SingleResource", typeName(actualData)));
        }
        assertResource(expectedResource, actualResource, policy);
      }
      case DocumentData.ResourceCollection(List<ResourceObject> expectedResources) -> {
        if (!(actualData
            instanceof DocumentData.ResourceCollection(List<ResourceObject> actualResources))) {
          throw fail(
              "expected resource-collection primary data"
                  + expectedButWas("ResourceCollection", typeName(actualData)));
        }
        if (expectedResources.size() != actualResources.size()) {
          throw fail(
              "resource collection size"
                  + expectedButWas(expectedResources.size(), actualResources.size()));
        }
        for (int i = 0; i < expectedResources.size(); i++) {
          assertResource(expectedResources.get(i), actualResources.get(i), policy);
        }
      }
      default -> throw fail("unsupported expected primary data: " + typeName(expectedData));
    }
    assertEqual("document.meta", expected.meta(), actual.meta());
    assertEqual("document.jsonapi", expected.jsonapi(), actual.jsonapi());
    assertEqual("document.links", expected.links(), actual.links());
    assertIncluded(expected.included(), actual.included(), policy);
    assertEqual(
        "document.additionalMembers", expected.additionalMembers(), actual.additionalMembers());
  }

  private static void assertIncluded(
      @Nullable List<ResourceObject> expected,
      @Nullable List<ResourceObject> actual,
      DomainWriteComparisonPolicy policy) {
    if (expected == null || actual == null) {
      assertEqual("document.included", expected, actual);
      return;
    }
    if (expected.size() != actual.size()) {
      throw fail("included size" + expectedButWas(expected.size(), actual.size()));
    }
    for (int i = 0; i < expected.size(); i++) {
      assertResource(expected.get(i), actual.get(i), policy);
    }
  }

  private static void assertAttributes(@Nullable Attributes expected, @Nullable Attributes actual) {
    if (expected == null || actual == null) {
      assertEqual("attributes", expected, actual);
      return;
    }
    assertEqual("attributes", expected.attributes(), actual.attributes());
  }

  private static void assertRelationships(
      @Nullable Relationships expected,
      @Nullable Relationships actual,
      DomainWriteComparisonPolicy policy) {
    if (expected == null || actual == null) {
      assertEqual("relationships", expected, actual);
      return;
    }
    Map<String, Relationship> expectedMap = expected.relationships();
    Map<String, Relationship> actualMap = actual.relationships();
    assertEqual("relationship names", expectedMap.keySet(), actualMap.keySet());
    for (Map.Entry<String, Relationship> entry : expectedMap.entrySet()) {
      String name = entry.getKey();
      Relationship expectedRelationship = entry.getValue();
      Relationship actualRelationship = Objects.requireNonNull(actualMap.get(name), name);
      assertEqual(name + ".links", expectedRelationship.links(), actualRelationship.links());
      assertEqual(name + ".meta", expectedRelationship.meta(), actualRelationship.meta());
      assertEqual(
          name + ".additionalMembers",
          expectedRelationship.additionalMembers(),
          actualRelationship.additionalMembers());
      assertLinkage(
          name, expectedRelationship.data(), actualRelationship.data(), policy.orderFor(name));
    }
  }

  private static void assertLinkage(
      String relationshipName,
      @Nullable RelationshipData expected,
      @Nullable RelationshipData actual,
      DomainWriteComparisonPolicy.ComparisonOrder order) {
    if (expected == null || actual == null) {
      assertEqual(relationshipName + ".data", expected, actual);
      return;
    }
    switch (expected) {
      case RelationshipData.NullLinkage ignored
          when actual instanceof RelationshipData.NullLinkage -> {
        // Both sides are explicit JSON null linkage; there is no nested identifier state to
        // compare.
      }
      case RelationshipData.NullLinkage ignored ->
          throw fail(
              relationshipName
                  + " expected NullLinkage"
                  + expectedButWas("NullLinkage", typeName(actual)));
      case RelationshipData.SingleLinkage(ResourceIdentifier expectedId)
          when actual instanceof RelationshipData.SingleLinkage(ResourceIdentifier actualId) ->
          assertEqual(relationshipName + ".identifier", expectedId, actualId);
      case RelationshipData.SingleLinkage ignored ->
          throw fail(
              relationshipName
                  + " expected SingleLinkage"
                  + expectedButWas("SingleLinkage", typeName(actual)));
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> expectedIds)
          when actual
              instanceof
              RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> actualIds) ->
          assertIdentifierCollection(relationshipName, expectedIds, actualIds, order);
      case RelationshipData.IdentifierCollectionLinkage ignored ->
          throw fail(
              relationshipName
                  + " expected IdentifierCollectionLinkage"
                  + expectedButWas("IdentifierCollectionLinkage", typeName(actual)));
      default -> throw fail(relationshipName + " unsupported linkage " + typeName(expected));
    }
  }

  private static void assertIdentifierCollection(
      String relationshipName,
      List<ResourceIdentifier> expectedIdentifiers,
      List<ResourceIdentifier> actualIdentifiers,
      DomainWriteComparisonPolicy.ComparisonOrder order) {
    if (expectedIdentifiers.size() != actualIdentifiers.size()) {
      throw fail(
          relationshipName
              + " linkage size"
              + expectedButWas(expectedIdentifiers.size(), actualIdentifiers.size()));
    }
    if (order == DomainWriteComparisonPolicy.ComparisonOrder.UNORDERED_IDENTIFIER_PAIRS) {
      if (!new HashSet<>(expectedIdentifiers).equals(new HashSet<>(actualIdentifiers))) {
        throw fail(relationshipName + " unordered identifiers: expected " + expectedIdentifiers);
      }
      return;
    }
    assertEqual(relationshipName + ".identifiers", expectedIdentifiers, actualIdentifiers);
  }

  private static void assertEqual(
      String label, @Nullable Object expected, @Nullable Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw fail(label + ":" + expectedButWas(expected, actual));
    }
  }

  private static String forScenario(String id) {
    return " for " + id;
  }

  private static String expectedButWas(@Nullable Object expected, @Nullable Object actual) {
    return " expected " + expected + " but was " + actual;
  }

  private static String typeName(@Nullable Object value) {
    return value == null ? "null" : value.getClass().getName();
  }

  private static AssertionError fail(String message) {
    return new AssertionError(message);
  }

  private static AssertionError fail(String message, Throwable cause) {
    return new AssertionError(message, cause);
  }
}
