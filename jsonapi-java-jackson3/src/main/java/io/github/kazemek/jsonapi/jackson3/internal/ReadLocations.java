package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson3.SourceLocation;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamLocation;

/** Converts Jackson locations into safe {@link SourceLocation} values. */
public final class ReadLocations {

  private ReadLocations() {}

  /** Current input cursor — use for EOF or when no token is present. */
  public static SourceLocation current(JsonParser parser) {
    return from(parser.currentLocation());
  }

  /** Start location of the current token — use when a token is present. */
  public static SourceLocation token(JsonParser parser) {
    return from(parser.currentTokenLocation());
  }

  public static SourceLocation from(@Nullable TokenStreamLocation location) {
    if (location == null || location == TokenStreamLocation.NA) {
      return SourceLocation.UNKNOWN;
    }
    return new SourceLocation(
        location.getLineNr(),
        location.getColumnNr(),
        location.getCharOffset(),
        location.getByteOffset());
  }
}
