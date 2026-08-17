package io.github.kazemek.jsonapi.testfixtures.domainpatch;

/** Shared PATCH request JSON documents for fixture catalogs and adapter contract tests. */
public final class PatchDocuments {

  /** Minimal article update supplying only {@code title}. */
  public static final String ARTICLE_TITLE_HELLO =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"title\":\"Hello\"}}}";

  /** Article update supplying the renamed {@code body-text} attribute. */
  public static final String ARTICLE_BODY_TEXT_CONTENT =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"attributes\":{\"body-text\":\"Content\"}}}";

  /** Article update replacing the {@code comments} collection relationship. */
  public static final String ARTICLE_COMMENTS_ONE =
      "{\"data\":{\"type\":\"articles\",\"id\":\"1\",\"relationships\":{\"comments\":{\"data\":[{\"type\":\"comments\",\"id\":\"c1\"}]}}}}";

  private PatchDocuments() {}
}
