package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.ArrayList;
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
 *
 * <p>The typed PATCH DTO path may supply a {@link FailurePathTranslator} so deep Jackson
 * construction-failure paths are translated to wire-name pointers through the structured-value
 * shape metadata (ADR-014); the flat DTO path keeps the innermost-property path.
 */
final class BeanConstruction {

  /**
   * Translates a failed bean construction's Jackson path into a resource-relative property path.
   */
  @FunctionalInterface
  interface FailurePathTranslator {
    String translate(Throwable failure, Class<?> rawType);
  }

  private BeanConstruction() {}

  static Object convertBean(
      JsonMapper mapper,
      Map<String, @Nullable Object> properties,
      JavaType targetType,
      Class<?> rawType) {
    return convertBean(mapper, properties, targetType, rawType, null);
  }

  static Object convertBean(
      JsonMapper mapper,
      Map<String, @Nullable Object> properties,
      JavaType targetType,
      Class<?> rawType,
      @Nullable FailurePathTranslator translator) {
    try {
      return mapper.convertValue(properties, targetType);
    } catch (RuntimeException e) {
      Throwable failure = jacksonFailure(e);
      MappingDiagnostic diagnostic =
          isCreatorInputFailure(failure)
              ? MappingDiagnostic.MISSING_CREATOR_INPUT
              : MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE;
      String path =
          translator != null ? translator.translate(failure, rawType) : propertyPath(failure);
      throw new JsonApiMappingException(
          diagnostic,
          rawType,
          path,
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

  /** The non-null property names of the Jackson failure path, outermost first. */
  static List<String> pathNames(Throwable failure) {
    if (failure instanceof JacksonException jackson) {
      List<JacksonException.Reference> path = jackson.getPath();
      if (path != null) {
        List<String> names = new ArrayList<>();
        for (JacksonException.Reference reference : path) {
          String name = reference.getPropertyName();
          if (name != null && !name.isEmpty()) {
            names.add(name);
          }
        }
        return names;
      }
    }
    return List.of();
  }

  static String propertyPath(Throwable failure) {
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
