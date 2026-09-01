package io.github.kazemek.jsonapi.testsupport.domainread;

import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Adapter-neutral semantic comparison for {@link DomainReadScenario} outcomes. Jackson-major suites
 * invoke their own binder, then hand the bound value or thrown mapping exception here.
 * Adapter-local cause types and Jackson-derived path details stay in the adapter spec.
 */
public final class DomainReadVerifier {

  private DomainReadVerifier() {}

  /**
   * Asserts that {@code result} and {@code thrown} match {@code scenario}'s discriminated
   * expectation.
   *
   * @throws AssertionError when the observed result diverges from the catalog expectation
   */
  public static void verify(
      DomainReadScenario scenario, @Nullable Object result, @Nullable Throwable thrown) {
    Objects.requireNonNull(scenario, "scenario");
    DomainReadExpectation expectation = scenario.expectation();
    if (expectation
        instanceof
        DomainReadExpectation.Failure(var diagnostic, var propertyPath, var resourceClass)) {
      if (!(thrown instanceof JsonApiMappingException exception)) {
        throw fail(
            "expected JsonApiMappingException for "
                + scenario.id()
                + expectedButWas("JsonApiMappingException", typeName(thrown)));
      }
      assertEqual("diagnostic", diagnostic, exception.diagnostic());
      if (propertyPath != null) {
        assertEqual("propertyPath", propertyPath, exception.propertyPath());
      }
      if (resourceClass != null) {
        assertEqual("resourceClass", resourceClass, exception.resourceClass());
      }
      return;
    }
    if (thrown != null) {
      throw fail("unexpected " + thrown.getClass().getName() + " for " + scenario.id(), thrown);
    }
    Object expected = ((DomainReadExpectation.BoundValue) expectation).value();
    if (scenario.input() instanceof DomainReadInput.IncludedIsolation) {
      if (!(result instanceof List<?> bound)) {
        throw fail(
            "expected a two-element bound list for " + scenario.id() + ", was " + typeName(result));
      }
      if (bound.size() != 2) {
        throw fail("included-isolation result size" + expectedButWas(2, bound.size()));
      }
      assertBoundValue(expected, bound.get(0));
      assertBoundValue(expected, bound.get(1));
      return;
    }
    assertBoundValue(expected, result);
  }

  static void assertBoundValue(Object expected, @Nullable Object actual) {
    if (expected instanceof List<?> expectedList) {
      if (!(actual instanceof List<?> actualList)) {
        throw fail("expected List" + expectedButWas("List", typeName(actual)));
      }
      if (expectedList.size() != actualList.size()) {
        throw fail("bound list size" + expectedButWas(expectedList.size(), actualList.size()));
      }
      for (int i = 0; i < expectedList.size(); i++) {
        assertBoundValue(expectedList.get(i), actualList.get(i));
      }
      return;
    }
    assertEqual("bound value", expected, actual);
  }

  private static void assertEqual(
      String label, @Nullable Object expected, @Nullable Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw fail(label + ":" + expectedButWas(expected, actual));
    }
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
