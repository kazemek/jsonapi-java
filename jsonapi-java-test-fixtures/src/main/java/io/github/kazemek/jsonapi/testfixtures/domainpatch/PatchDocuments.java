package io.github.kazemek.jsonapi.testfixtures.domainpatch;

/** Shared PATCH request JSON documents for fixture catalogs and adapter contract tests. */
public final class PatchDocuments {

  /** Minimal article update supplying only {@code title}. */
  public static final String ARTICLE_TITLE_HELLO =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"Hello\"}}}";

  private PatchDocuments() {}
}
