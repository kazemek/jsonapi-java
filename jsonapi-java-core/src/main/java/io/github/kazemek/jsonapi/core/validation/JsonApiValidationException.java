package io.github.kazemek.jsonapi.core.validation;

/** Validation failure carrying a stable rule code and JSON Pointer-like path. */
public final class JsonApiValidationException extends RuntimeException {

  private final ValidationRuleCode ruleCode;
  private final String jsonPointer;

  public JsonApiValidationException(
      ValidationRuleCode ruleCode, String jsonPointer, String message) {
    super(message);
    this.ruleCode = ruleCode;
    this.jsonPointer = jsonPointer;
  }

  public ValidationRuleCode ruleCode() {
    return ruleCode;
  }

  public String jsonPointer() {
    return jsonPointer;
  }
}
