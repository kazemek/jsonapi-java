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
    if (outcome instanceof DecorationOutcome.Failure failure) {
      if (thrown == null) {
        throw new AssertionError(
            "Decoration scenario '"
                + scenario.id()
                + "' expected failure "
                + failure.expectedDiagnostic()
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
      if (mappingException.diagnostic() != failure.expectedDiagnostic()) {
        throw new AssertionError(
            "Decoration scenario '"
                + scenario.id()
                + "' expected diagnostic "
                + failure.expectedDiagnostic()
                + " but was "
                + mappingException.diagnostic(),
            thrown);
      }
      return;
    }
    if (thrown != null) {
      throw new AssertionError(
          "Decoration scenario '" + scenario.id() + "' unexpected exception", thrown);
    }
    if (outcome instanceof DecorationOutcome.ResourceSuccess success) {
      if (!(result instanceof ResourceObject actual)) {
        throw new AssertionError(
            "Decoration scenario '"
                + scenario.id()
                + "' expected ResourceObject but was "
                + (result == null ? "null" : result.getClass().getName()));
      }
      assertResource(success.expected(), actual, scenario.id());
      return;
    }
    if (outcome instanceof DecorationOutcome.DocumentSuccess success) {
      if (!(result instanceof JsonApiDocument actual)) {
        throw new AssertionError(
            "Decoration scenario '"
                + scenario.id()
                + "' expected JsonApiDocument but was "
                + (result == null ? "null" : result.getClass().getName()));
      }
      assertDocument(success.expectedPrimary(), success.expectedIncluded(), actual, scenario.id());
      return;
    }
    if (outcome instanceof DecorationOutcome.MappedDocumentSuccess success) {
      if (!(result instanceof MappedDocument actual)) {
        throw new AssertionError(
            "Decoration scenario '"
                + scenario.id()
                + "' expected MappedDocument but was "
                + (result == null ? "null" : result.getClass().getName()));
      }
      assertDocument(
          success.expectedPrimary(), success.expectedIncluded(), actual.document(), scenario.id());
      return;
    }
    throw new AssertionError("Unknown outcome for scenario " + scenario.id());
  }

  /** Legacy verifier for resource-only scenarios. */
  public static void verify(DecorationScenario scenario, ResourceObject actual) {
    verify(scenario, (Object) actual, null);
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
    if (!(actual.data() instanceof DocumentData.SingleResource single)) {
      throw new AssertionError(
          "Decoration scenario '" + scenarioId + "' expected single-resource document");
    }
    assertResource(expectedPrimary, single.resource(), scenarioId);
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
