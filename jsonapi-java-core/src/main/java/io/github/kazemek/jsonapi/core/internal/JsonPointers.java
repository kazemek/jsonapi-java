package io.github.kazemek.jsonapi.core.internal;

/** RFC 6901 JSON Pointer path helpers. These are document pointers, not filesystem paths. */
public final class JsonPointers {

  private JsonPointers() {}

  /** Returns {@code /segment} for an empty prefix, otherwise {@code prefix/segment}. */
  public static String child(String prefix, String segment) {
    if (prefix == null || prefix.isEmpty()) {
      return root(segment);
    }
    return prefix + "/" + segment; // NOSONAR java:S1075 - JSON Pointer delimiter, not a file path
  }

  public static String root(String segment) {
    return "/" + segment; // NOSONAR java:S1075 - JSON Pointer delimiter, not a file path
  }
}
