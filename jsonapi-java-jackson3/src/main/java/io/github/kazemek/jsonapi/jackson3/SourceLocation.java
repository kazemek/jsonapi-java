package io.github.kazemek.jsonapi.jackson3;

/**
 * Safe source location for read diagnostics (line, column, and offsets only — never source text).
 */
public record SourceLocation(int lineNumber, int columnNumber, long charOffset, long byteOffset) {

  /** Location when Jackson did not report a usable position. */
  public static final SourceLocation UNKNOWN = new SourceLocation(-1, -1, -1L, -1L);

  public boolean isKnown() {
    return lineNumber >= 0 || columnNumber >= 0 || charOffset >= 0 || byteOffset >= 0;
  }
}
