package io.github.kazemek.jsonapi.testsupport.domainpatch;

import io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Adapter-neutral semantic comparison for {@link PatchDtoScenario} outcomes. Jackson-major suites
 * invoke their own PATCH DTO reader, then hand the bound DTO or caught exception here.
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
    if (expectation instanceof PatchDtoExpectation.Success(Object identity, var members)) {
      if (result == null || !scenario.targetType().isInstance(result)) {
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
    if (expectation instanceof PatchDtoExpectation.ReaderFailure(var code, String jsonPointer)) {
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
    if (expectation
        instanceof PatchDtoExpectation.BinderFailure(var diagnostic, String propertyPath)) {
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
    throw fail("unknown expectation " + typeName(expectation));
  }

  static @Nullable Object readMember(Object dto, String name) {
    try {
      return dto.getClass().getMethod(name).invoke(dto);
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
      throw fail("cannot read member " + name + " on " + dto.getClass().getName(), ex);
    }
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
