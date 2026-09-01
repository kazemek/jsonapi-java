package io.github.kazemek.jsonapi.testsupport.decoration;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.mapping.MappedDocument;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Adapter-neutral verifier for {@link DecorationScenario} outcomes. Jackson-major suites invoke
 * their own mapper, then hand the result here so Jackson 2 does not copy resource comparison.
 */
public final class DecorationVerifier {

  private DecorationVerifier() {}

  public static void verify(
      DecorationScenario scenario, @Nullable Object result, @Nullable Throwable thrown) {
    Objects.requireNonNull(scenario, "scenario");
    DecorationOutcome outcome = scenario.outcome();
    if (outcome instanceof DecorationOutcome.Failure(var expectedDiagnostic)) {
      verifyFailure(scenario, expectedDiagnostic, thrown);
      return;
    }
    if (thrown != null) {
      throw new AssertionError(
          "Decoration scenario '" + scenario.id() + "' unexpected exception", thrown);
    }
    switch (outcome) {
      case DecorationOutcome.ResourceSuccess(var expected) ->
          verifyResourceScenario(scenario, expected, result);
      case DecorationOutcome.DocumentSuccess(var expectedPrimary, var expectedIncluded) ->
          verifyDocumentScenario(scenario, expectedPrimary, expectedIncluded, result);
      case DecorationOutcome.MappedDocumentSuccess(var expectedPrimary, var expectedIncluded) ->
          verifyMappedScenario(scenario, expectedPrimary, expectedIncluded, result);
      case DecorationOutcome.Failure ignored -> throw new AssertionError("unreachable");
    }
  }

  private static void verifyFailure(
      DecorationScenario scenario, Object expectedDiagnostic, @Nullable Throwable thrown) {
    if (thrown == null) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenario.id()
              + "' expected failure "
              + expectedDiagnostic
              + " but no exception was thrown");
    }
    if (!(thrown instanceof JsonApiMappingException mappingException)) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenario.id()
              + "' expected JsonApiMappingException but was "
              + thrown.getClass().getName(),
          thrown);
    }
    if (mappingException.diagnostic() != expectedDiagnostic) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenario.id()
              + "' expected diagnostic "
              + expectedDiagnostic
              + " but was "
              + mappingException.diagnostic(),
          thrown);
    }
  }

  private static void verifyResourceScenario(
      DecorationScenario scenario, ResourceObject expected, @Nullable Object result) {
    if (!(result instanceof ResourceObject actual)) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenario.id()
              + "' expected ResourceObject but was "
              + (result == null ? "null" : result.getClass().getName()));
    }
    assertResource(expected, actual, scenario.id());
  }

  private static void verifyDocumentScenario(
      DecorationScenario scenario,
      ResourceObject expectedPrimary,
      @Nullable List<ResourceObject> expectedIncluded,
      @Nullable Object result) {
    if (!(result instanceof JsonApiDocument actual)) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenario.id()
              + "' expected JsonApiDocument but was "
              + (result == null ? "null" : result.getClass().getName()));
    }
    assertDocument(expectedPrimary, expectedIncluded, actual, scenario.id());
  }

  private static void verifyMappedScenario(
      DecorationScenario scenario,
      ResourceObject expectedPrimary,
      @Nullable List<ResourceObject> expectedIncluded,
      @Nullable Object result) {
    if (!(result instanceof MappedDocument actual)) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenario.id()
              + "' expected MappedDocument but was "
              + (result == null ? "null" : result.getClass().getName()));
    }
    assertDocument(expectedPrimary, expectedIncluded, actual.document(), scenario.id());
  }

  /** Legacy verifier for resource-only scenarios. */
  public static void verify(DecorationScenario scenario, ResourceObject actual) {
    verify(scenario, actual, null);
  }

  private static void assertResource(
      ResourceObject expected, ResourceObject actual, String scenarioId) {
    if (!expected.type().equals(actual.type())
        || !Objects.equals(expected.id(), actual.id())
        || !Objects.equals(expected.lid(), actual.lid())
        || !Objects.equals(expected.attributes(), actual.attributes())
        || !Objects.equals(expected.relationships(), actual.relationships())
        || !Objects.equals(expected.links(), actual.links())
        || !Objects.equals(expected.meta(), actual.meta())
        || !Objects.equals(expected.additionalMembers(), actual.additionalMembers())) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenarioId
              + "' mismatch: expected links "
              + expected.links()
              + " relationships "
              + expected.relationships()
              + " but was links "
              + actual.links()
              + " relationships "
              + actual.relationships());
    }
  }

  private static void assertDocument(
      ResourceObject expectedPrimary,
      @Nullable List<ResourceObject> expectedIncluded,
      JsonApiDocument actual,
      String scenarioId) {
    if (!(actual.data() instanceof DocumentData.SingleResource(var resource))) {
      throw new AssertionError(
          "Decoration scenario '" + scenarioId + "' expected single-resource document");
    }
    assertResource(expectedPrimary, resource, scenarioId);
    List<ResourceObject> actualIncluded = actual.included();
    if (expectedIncluded == null) {
      if (actualIncluded != null) {
        throw new AssertionError(
            "Decoration scenario '"
                + scenarioId
                + "' expected no included but was "
                + actualIncluded);
      }
    } else {
      if (actualIncluded == null || actualIncluded.size() != expectedIncluded.size()) {
        throw new AssertionError(
            "Decoration scenario '"
                + scenarioId
                + "' included size mismatch: expected "
                + expectedIncluded.size()
                + " but was "
                + (actualIncluded == null ? "null" : actualIncluded.size()));
      }
      for (int i = 0; i < expectedIncluded.size(); i++) {
        assertResource(
            expectedIncluded.get(i), actualIncluded.get(i), scenarioId + " included[" + i + "]");
      }
    }
  }
}
