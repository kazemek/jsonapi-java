package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiDocumentReadException;
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.patch.PatchChange;
import io.github.kazemek.jsonapi.jackson.patch.PatchCommand;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Adapter-neutral semantic comparison for {@link PatchScenario} outcomes, including array-valued
 * {@link PatchChange} members whose record equality is identity-based.
 */
public final class PatchVerifier {

  private PatchVerifier() {}

  /**
   * Asserts that {@code result} matches {@code scenario}'s discriminated expectation. Adapter
   * runners catch reader/binder failures and pass them as {@code result}.
   *
   * @throws AssertionError when the observed result diverges from the catalog expectation
   */
  public static void verify(PatchScenario scenario, @Nullable Object result) {
    Objects.requireNonNull(scenario, "scenario");
    PatchExpectation expectation = scenario.expectation();
    switch (expectation) {
      case PatchExpectation.Success(Object identity, List<PatchChange> changes) -> {
        if (!(result
            instanceof
            PatchCommand(
                Class<?> resourceType,
                Object actualIdentity,
                List<PatchChange> actualChanges))) {
          throw fail("expected PatchCommand for " + scenario.id() + was(typeName(result)));
        }
        assertEqual("resourceType", scenario.targetType(), resourceType);
        assertEqual("identity", identity, actualIdentity);
        assertChanges(changes, actualChanges);
        return;
      }
      case PatchExpectation.ReaderFailure(var code, String jsonPointer) -> {
        if (!(result instanceof JsonApiDocumentReadException exception)) {
          throw fail(
              "expected JsonApiDocumentReadException for " + scenario.id() + was(typeName(result)));
        }
        assertEqual("ruleCode", code, exception.ruleCode());
        assertEqual("jsonPointer", jsonPointer, exception.jsonPointer());
        return;
      }
      case PatchExpectation.BinderFailure(var diagnostic, String propertyPath) -> {
        if (!(result instanceof JsonApiMappingException exception)) {
          throw fail(
              "expected JsonApiMappingException for " + scenario.id() + was(typeName(result)));
        }
        assertEqual("diagnostic", diagnostic, exception.diagnostic());
        assertEqual("propertyPath", propertyPath, exception.propertyPath());
        return;
      }
      default -> {
        // Intentionally empty: unknown expectation falls through to failure below.
      }
    }
    throw fail("unknown expectation " + typeName(expectation));
  }

  static void assertChanges(List<PatchChange> expected, List<PatchChange> actual) {
    if (expected.size() != actual.size()) {
      throw fail("change count" + expectedButWas(expected.size(), actual.size()));
    }
    for (int i = 0; i < expected.size(); i++) {
      if (!changeEqual(expected.get(i), actual.get(i))) {
        throw fail("change[" + i + "]" + expectedButWas(expected.get(i), actual.get(i)));
      }
    }
  }

  static boolean changeEqual(PatchChange expected, PatchChange actual) {
    if (expected.getClass() != actual.getClass()) {
      return false;
    }
    return Objects.equals(expected.jsonapiName(), actual.jsonapiName())
        && Objects.equals(expected.logicalName(), actual.logicalName())
        && valuesEqual(expected.value(), actual.value());
  }

  static boolean valuesEqual(@Nullable Object expected, @Nullable Object actual) {
    if (expected == actual) {
      return true;
    }
    if (expected == null || actual == null) {
      return false;
    }
    if (expected.getClass().isArray() && actual.getClass().isArray()) {
      if (expected instanceof Object[] expectedObjects
          && actual instanceof Object[] actualObjects) {
        return Arrays.deepEquals(expectedObjects, actualObjects);
      }
      int expectedLength = Array.getLength(expected);
      int actualLength = Array.getLength(actual);
      if (expectedLength != actualLength) {
        return false;
      }
      for (int i = 0; i < expectedLength; i++) {
        if (!Objects.equals(Array.get(expected, i), Array.get(actual, i))) {
          return false;
        }
      }
      return true;
    }
    return Objects.equals(expected, actual);
  }

  private static void assertEqual(
      String label, @Nullable Object expected, @Nullable Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw fail(label + ":" + expectedButWas(expected, actual));
    }
  }

  private static String was(String actual) {
    return ", was " + actual;
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
}
