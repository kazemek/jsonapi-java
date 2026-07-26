package io.github.kazemek.jsonapi.core.internal;

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Null-preserving ordered map and list copies. */
public final class OrderedMaps {

  private OrderedMaps() {}

  public static <K, V> Map<K, V> copyOfNullableValues(Map<K, V> source) {
    if (source == null || source.isEmpty()) {
      return Map.of();
    }
    return Collections.unmodifiableMap(new LinkedHashMap<>(source));
  }

  public static <E> List<E> copyOfNullableElements(List<E> source) {
    if (source == null || source.isEmpty()) {
      return List.of();
    }
    return Collections.unmodifiableList(new ArrayList<>(source));
  }

  public static <K, V> void requireNoCollisions(
      Map<K, V> primary, Map<K, V> secondary, String containerName, String path) {
    for (K key : secondary.keySet()) {
      if (primary.containsKey(key)) {
        throw new JsonApiValidationException(
            ValidationRuleCode.MEMBER_NAME_COLLISION,
            path + "/" + key,
            "Member name collision in " + containerName + ": " + key);
      }
    }
  }

  public static void rejectReservedNames(
      Map<String, ?> members, Set<String> reserved, String pathPrefix, String messagePrefix) {
    if (members == null || members.isEmpty()) {
      return;
    }
    for (String name : members.keySet()) {
      if (reserved.contains(name)) {
        String path = pathPrefix.isEmpty() ? "/" + name : pathPrefix + "/" + name;
        LocalValidation.fail(ValidationRuleCode.RESERVED_FIELD_NAME, path, messagePrefix + name);
      }
    }
  }
}
