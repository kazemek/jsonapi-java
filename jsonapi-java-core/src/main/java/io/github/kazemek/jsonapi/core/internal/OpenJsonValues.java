package io.github.kazemek.jsonapi.core.internal;

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Validation and deep-copy of JSON-compatible open values. */
public final class OpenJsonValues {

  private OpenJsonValues() {}

  public static boolean isValid(Object value) {
    return switch (value) {
      case null -> true;
      case String ignored -> true;
      case Boolean ignored -> true;
      case Number number -> isFiniteNumber(number);
      case List<?> list -> list.stream().allMatch(OpenJsonValues::isValid);
      case Map<?, ?> map -> isValidMap(map);
      default -> false;
    };
  }

  public static Object copy(Object value) {
    return copy(value, "");
  }

  public static Object copy(Object value, String path) {
    return switch (value) {
      case null -> null;
      case String s -> s;
      case Boolean b -> b;
      case Number number -> copyNumber(number, path);
      case List<?> list -> copyList(list, path);
      case Map<?, ?> map -> copyMapValue(map, path);
      default ->
          throw new JsonApiValidationException(
              ValidationRuleCode.INVALID_OPEN_JSON_VALUE,
              path,
              "Unsupported JSON value type: " + value.getClass().getName());
    };
  }

  public static Map<String, Object> copyMap(Map<String, ?> source) {
    return copyMap(source, "");
  }

  public static Map<String, Object> copyMap(Map<String, ?> source, String path) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    Map<String, Object> copy = new LinkedHashMap<>();
    for (Map.Entry<String, ?> entry : source.entrySet()) {
      copy.put(entry.getKey(), copy(entry.getValue(), path + "/" + entry.getKey()));
    }
    return OrderedMaps.copyOfNullableValues(copy);
  }

  public static List<String> copyStringList(List<String> source) {
    if (source == null || source.isEmpty()) {
      return List.of();
    }
    return List.copyOf(source);
  }

  private static boolean isValidMap(Map<?, ?> map) {
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (!(entry.getKey() instanceof String) || !isValid(entry.getValue())) {
        return false;
      }
    }
    return true;
  }

  private static Number copyNumber(Number number, String path) {
    if (!isFiniteNumber(number)) {
      throw new JsonApiValidationException(
          ValidationRuleCode.INVALID_OPEN_JSON_VALUE, path, "Non-finite number: " + number);
    }
    return number;
  }

  private static List<Object> copyList(List<?> list, String path) {
    List<Object> copy = new ArrayList<>(list.size());
    for (int i = 0; i < list.size(); i++) {
      copy.add(copy(list.get(i), path + "/" + i));
    }
    return OrderedMaps.copyOfNullableElements(copy);
  }

  private static Map<String, Object> copyMapValue(Map<?, ?> map, String path) {
    Map<String, Object> copy = new LinkedHashMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet()) {
      if (!(entry.getKey() instanceof String key)) {
        throw new JsonApiValidationException(
            ValidationRuleCode.INVALID_OPEN_JSON_VALUE, path, "Map keys must be strings");
      }
      copy.put(key, copy(entry.getValue(), path + "/" + key));
    }
    return OrderedMaps.copyOfNullableValues(copy);
  }

  private static boolean isFiniteNumber(Number number) {
    return switch (number) {
      case Double d -> !d.isInfinite() && !d.isNaN();
      case Float f -> !f.isInfinite() && !f.isNaN();
      default -> true;
    };
  }
}
