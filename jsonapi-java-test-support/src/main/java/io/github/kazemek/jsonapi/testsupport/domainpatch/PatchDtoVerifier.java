package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiDocumentReadException;
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Adapter-neutral semantic comparison for {@link PatchDtoScenario} outcomes. Jackson-major suites
 * invoke their own PATCH DTO reader, then hand the bound DTO or caught exception here.
 *
 * <p>Member comparison is presence-aware: {@link PatchPresence.Omitted} is distinct from {@link
 * PatchPresence.Present}, including {@code Present(null)}. Array payloads compare by contents, and
 * nested presence-aware PATCH DTO records compare each component the same way, because generated
 * record equality is identity-based for arrays.
 */
public final class PatchDtoVerifier {

  private PatchDtoVerifier() {}

  /**
   * Asserts that {@code result} matches {@code scenario}'s discriminated expectation.
   *
   * @throws AssertionError when the observed result diverges from the catalog expectation
   */
  public static void verify(PatchDtoScenario scenario, @Nullable Object result) {
    Objects.requireNonNull(scenario, "scenario");
    PatchDtoExpectation expectation = scenario.expectation();
    switch (expectation) {
      case PatchDtoExpectation.Success(Object identity, var members) -> {
        if (!scenario.targetType().isInstance(result)) {
          throw fail(
              "expected "
                  + scenario.targetType().getName()
                  + " for "
                  + scenario.id()
                  + expectedButWas(scenario.targetType().getName(), typeName(result)));
        }
        assertEqual("identity", identity, readMember(result, "id"));
        for (Map.Entry<String, PatchPresence<?>> entry : members.entrySet()) {
          assertEqual(
              "member " + entry.getKey(), entry.getValue(), readMember(result, entry.getKey()));
        }
        return;
      }
      case PatchDtoExpectation.ReaderFailure(var code, String jsonPointer) -> {
        if (!(result instanceof JsonApiDocumentReadException exception)) {
          throw fail(
              "expected JsonApiDocumentReadException for "
                  + scenario.id()
                  + expectedButWas("JsonApiDocumentReadException", typeName(result)));
        }
        assertEqual("ruleCode", code, exception.ruleCode());
        assertEqual("jsonPointer", jsonPointer, exception.jsonPointer());
        return;
      }
      case PatchDtoExpectation.BinderFailure(var diagnostic, String propertyPath) -> {
        if (!(result instanceof JsonApiMappingException exception)) {
          throw fail(
              "expected JsonApiMappingException for "
                  + scenario.id()
                  + expectedButWas("JsonApiMappingException", typeName(result)));
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

  static @Nullable Object readMember(Object dto, String name) {
    try {
      return dto.getClass().getMethod(name).invoke(dto);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
      throw fail("cannot read member " + name + " on " + dto.getClass().getName(), ex);
    }
  }

  static boolean membersEqual(@Nullable Object expected, @Nullable Object actual) {
    if (expected == actual) {
      return true;
    }
    if (expected == null || actual == null) {
      return false;
    }
    if (expected instanceof PatchPresence<?> expectedPresence
        && actual instanceof PatchPresence<?> actualPresence) {
      return presenceEqual(expectedPresence, actualPresence);
    }
    if (expected.getClass().isArray() && actual.getClass().isArray()) {
      return PatchVerifier.valuesEqual(expected, actual);
    }
    if (expected.getClass() == actual.getClass() && expected.getClass().isRecord()) {
      return recordPayloadsEqual(expected, actual);
    }
    return Objects.equals(expected, actual);
  }

  private static boolean presenceEqual(PatchPresence<?> expected, PatchPresence<?> actual) {
    if (expected instanceof PatchPresence.Omitted && actual instanceof PatchPresence.Omitted) {
      return true;
    }
    if (expected instanceof PatchPresence.Present(var expectedValue)
        && actual instanceof PatchPresence.Present(var actualValue)) {
      return membersEqual(expectedValue, actualValue);
    }
    return false;
  }

  private static boolean recordPayloadsEqual(Object expected, Object actual) {
    for (RecordComponent component : expected.getClass().getRecordComponents()) {
      Object expectedValue;
      Object actualValue;
      try {
        expectedValue = component.getAccessor().invoke(expected);
        actualValue = component.getAccessor().invoke(actual);
      } catch (IllegalAccessException | InvocationTargetException ex) {
        throw fail("cannot read record component " + component.getName(), ex);
      }
      if (!membersEqual(expectedValue, actualValue)) {
        return false;
      }
    }
    return true;
  }

  private static void assertEqual(
      String label, @Nullable Object expected, @Nullable Object actual) {
    if (!membersEqual(expected, actual)) {
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
