package io.github.kazemek.jsonapi.core.validation;

import java.util.ArrayList;
import java.util.List;

/** Local validation helper used by model compact constructors. */
public final class LocalValidation {

  private LocalValidation() {}

  public static void fail(ValidationRuleCode code, String path, String message) {
    throw new JsonApiValidationException(code, path, message);
  }

  /** Rejects a null required value with a stable rule code and JSON Pointer-like path. */
  public static <T> T requireNonNull(T value, String path, String message) {
    if (value == null) {
      fail(ValidationRuleCode.NULL_REQUIRED_VALUE, path, message);
    }
    return value;
  }

  /**
   * Defensively copies a required collection, rejecting a null payload and null elements with
   * stable rule codes and indexed paths.
   */
  public static <T> List<T> copyRequiredList(List<T> source, String path) {
    if (source == null) {
      fail(ValidationRuleCode.NULL_COLLECTION_PAYLOAD, path, "Collection payload must not be null");
    }
    List<T> copy = new ArrayList<>(source.size());
    for (int i = 0; i < source.size(); i++) {
      T element = source.get(i);
      if (element == null) {
        fail(
            ValidationRuleCode.NULL_COLLECTION_ELEMENT,
            path + "/" + i,
            "Collection element must not be null");
      }
      copy.add(element);
    }
    return List.copyOf(copy);
  }
}
