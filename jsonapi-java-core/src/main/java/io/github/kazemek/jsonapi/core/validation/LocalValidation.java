package io.github.kazemek.jsonapi.core.validation;

/** Local validation helper used by model compact constructors. */
public final class LocalValidation {

  private LocalValidation() {}

  public static void fail(ValidationRuleCode code, String path, String message) {
    throw new JsonApiValidationException(code, path, message);
  }
}
