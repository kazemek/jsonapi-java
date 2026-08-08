package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import io.github.kazemek.jsonapi.core.internal.SyntaxValidators;
import io.github.kazemek.jsonapi.core.validation.LocalValidation;
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Error object {@code source} members: optional JSON Pointer, parameter name, and header name.
 *
 * <p>When present, {@code pointer} must be RFC 6901 JSON Pointer syntax (syntax only; not resolved
 * against a document). See {@code docs/conformance.md}.
 */
public record ErrorSource(
    @Nullable String pointer,
    @Nullable String parameter,
    @Nullable String header,
    Map<String, @Nullable Object> additionalMembers) {

  private static final Set<String> RESERVED_ADDITIONAL =
      Set.of(JsonApiMembers.POINTER, JsonApiMembers.PARAMETER, JsonApiMembers.HEADER);

  public ErrorSource {
    if (pointer != null && !SyntaxValidators.isValidJsonPointer(pointer)) {
      LocalValidation.fail(
          ValidationRuleCode.INVALID_JSON_POINTER,
          "/errors/source/pointer",
          "Invalid JSON Pointer: " + pointer);
    }
    additionalMembers =
        AdditionalMembers.copy(
            additionalMembers,
            "/errors/source",
            "Invalid error source member name: ",
            RESERVED_ADDITIONAL);
  }

  public static ErrorSource ofParameter(String parameter) {
    return new ErrorSource(null, parameter, null, Map.of());
  }
}
