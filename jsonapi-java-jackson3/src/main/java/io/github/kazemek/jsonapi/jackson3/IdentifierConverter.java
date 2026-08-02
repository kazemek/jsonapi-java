package io.github.kazemek.jsonapi.jackson3;

import org.jspecify.annotations.Nullable;

/**
 * Converts a domain identifier value to its JSON:API string representation.
 *
 * <p>Implementations receive the raw identifier value from a {@code @JsonApiId} property and return
 * the string used as {@code "id"} in the resource object. Return {@code null} to signal a missing
 * identifier, which causes a {@link JsonApiMappingException} with {@link
 * MappingDiagnostic#MISSING_IDENTIFIER}.
 *
 * <p>Use {@link #defaults()} for the built-in {@code toString()} conversion.
 */
@FunctionalInterface
public interface IdentifierConverter {

  /**
   * Converts a raw identifier value to its string form. A {@code null} return signals a missing
   * identifier.
   */
  @Nullable String convert(@Nullable Object identifierValue);

  /** Returns the built-in converter that delegates to {@link Object#toString()}. */
  static IdentifierConverter defaults() {
    return identifierValue -> identifierValue != null ? identifierValue.toString() : null;
  }
}
