package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.ValueInstantiationException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Shared bean construction from a synthetic property map with a single {@link
 * JsonMapper#convertValue(Object, JavaType)}, plus stable creator/coercion failure classification
 * used by the flat DTO binder and the typed PATCH DTO binder.
 */
final class BeanConstruction {

  private BeanConstruction() {}

  static Object convertBean(
      JsonMapper mapper,
      Map<String, @Nullable Object> properties,
      JavaType targetType,
      Class<?> rawType) {
    try {
      return mapper.convertValue(properties, targetType);
    } catch (RuntimeException e) {
      Throwable failure = jacksonFailure(e);
      MappingDiagnostic diagnostic =
          isCreatorInputFailure(failure)
              ? MappingDiagnostic.MISSING_CREATOR_INPUT
              : MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE;
      throw new JsonApiMappingException(
          diagnostic,
          rawType,
          propertyPath(failure),
          "Failed to construct " + rawType.getName() + " from resource values",
          failure);
    }
  }

  /**
   * Classifies bulk {@code convertValue} failures as creator/instantiation input problems.
   *
   * <p>Jackson 3.2 reports missing creator properties as {@link MismatchedInputException} with the
   * "Missing required creator property" message, and creator/instantiation failures (including
   * throwing creators) as {@link ValueInstantiationException}. Both mean the bean could not be
   * constructed from the supplied inputs, so both map to {@link
   * MappingDiagnostic#MISSING_CREATOR_INPUT}; all other coercion, type, or property failures map to
   * {@link MappingDiagnostic#UNSUPPORTED_ATTRIBUTE_VALUE} (milestone Phase 2.9 contract).
   */
  private static boolean isCreatorInputFailure(Throwable failure) {
    if (failure instanceof ValueInstantiationException) {
      return true;
    }
    if (failure instanceof MismatchedInputException mismatched) {
      String message = mismatched.getMessage();
      return message != null && message.contains("Missing required creator property");
    }
    return false;
  }

  private static Throwable jacksonFailure(Throwable failure) {
    Throwable current = failure;
    while (current != null) {
      if (current instanceof JacksonException) {
        return current;
      }
      current = current.getCause();
    }
    return failure;
  }

  private static String propertyPath(Throwable failure) {
    if (failure instanceof JacksonException jackson) {
      List<JacksonException.Reference> path = jackson.getPath();
      if (path != null) {
        for (int i = path.size() - 1; i >= 0; i--) {
          String name = path.get(i).getPropertyName();
          if (name != null && !name.isEmpty()) {
            return "/" + name;
          }
        }
      }
    }
    return "/";
  }
}
