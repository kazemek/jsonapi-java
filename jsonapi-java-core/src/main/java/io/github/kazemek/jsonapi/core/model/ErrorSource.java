package io.github.kazemek.jsonapi.core.model;

import io.github.kazemek.jsonapi.core.internal.AdditionalMembers;
import java.util.Map;
import java.util.Set;

/** Error object source pointer. */
public record ErrorSource(
    String pointer, String parameter, String header, Map<String, Object> additionalMembers) {

  private static final Set<String> RESERVED_ADDITIONAL = Set.of("pointer", "parameter", "header");

  public ErrorSource {
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
