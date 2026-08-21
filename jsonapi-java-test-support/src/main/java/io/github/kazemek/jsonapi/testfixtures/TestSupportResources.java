package io.github.kazemek.jsonapi.testfixtures;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Classpath access to the shared JSON:API corpus and vendored schema resources owned by this
 * module. Paths are relative to {@link #CORPUS_ROOT} or {@link #SCHEMA_VENDOR_ROOT}; callers never
 * resolve repository working-directory locations.
 *
 * <p>{@link #readCorpusBytes(String)} and {@link #readSchemaBytes(String)} return the exact
 * resource bytes so codec tests can compare raw UTF-8 input.
 */
public final class TestSupportResources {

  /** Project-authored JSON:API 1.1 semantic corpus (positive, negative, envelope-binding). */
  public static final String CORPUS_ROOT = "jsonapi/corpus/1.1/";

  /** Vendored JSON:API 1.1 draft-PR schemas pinned under a reference namespace. */
  public static final String SCHEMA_VENDOR_ROOT = "jsonapi/schema/vendor/1.1-pr1603/";

  private TestSupportResources() {}

  /**
   * Exact bytes of a corpus resource. {@code relativePath} is corpus-relative, e.g. {@code
   * documents/member-order.compact.json}.
   */
  public static byte[] readCorpusBytes(String relativePath) {
    return readResource(CORPUS_ROOT + requireRelative(relativePath));
  }

  /** UTF-8 text of a corpus resource decoded from {@link #readCorpusBytes(String)}. */
  public static String readCorpusUtf8(String relativePath) {
    return utf8(readCorpusBytes(relativePath));
  }

  /** Stream over the exact corpus bytes; the caller closes the stream. */
  public static InputStream openCorpus(String relativePath) {
    return new ByteArrayInputStream(readCorpusBytes(relativePath));
  }

  public static boolean corpusExists(String relativePath) {
    return resourceUrl(CORPUS_ROOT + requireRelative(relativePath)) != null;
  }

  /**
   * Exact bytes of a vendored schema resource. {@code relativePath} is pin-directory-relative, e.g.
   * {@code schema.json} or {@code invalid-controls/response-missing-primary.json}.
   */
  public static byte[] readSchemaBytes(String relativePath) {
    return readResource(SCHEMA_VENDOR_ROOT + requireRelative(relativePath));
  }

  /** UTF-8 text of a vendored schema resource decoded from {@link #readSchemaBytes(String)}. */
  public static String readSchemaUtf8(String relativePath) {
    return utf8(readSchemaBytes(relativePath));
  }

  /** Stream over the exact schema bytes; the caller closes the stream. */
  public static InputStream openSchema(String relativePath) {
    return new ByteArrayInputStream(readSchemaBytes(relativePath));
  }

  public static boolean schemaExists(String relativePath) {
    return resourceUrl(SCHEMA_VENDOR_ROOT + requireRelative(relativePath)) != null;
  }

  private static String utf8(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static String requireRelative(String relativePath) {
    Objects.requireNonNull(relativePath, "relativePath");
    if (relativePath.isEmpty()
        || relativePath.charAt(0) == '/'
        || relativePath.charAt(0) == '\\'
        || relativePath.contains("..")) {
      throw new IllegalArgumentException("Invalid test-support resource path: " + relativePath);
    }
    return relativePath.replace('\\', '/');
  }

  private static byte[] readResource(String resourcePath) {
    URL url = resourceUrl(resourcePath);
    if (url == null) {
      throw new IllegalStateException("Missing test-support classpath resource: " + resourcePath);
    }
    try (InputStream in = url.openStream()) {
      return in.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Failed to read test-support classpath resource: " + resourcePath, e);
    }
  }

  private static URL resourceUrl(String resourcePath) {
    ClassLoader loader = TestSupportResources.class.getClassLoader();
    if (loader != null) {
      URL url = loader.getResource(resourcePath);
      if (url != null) {
        return url;
      }
    }
    return TestSupportResources.class.getResource("/" + resourcePath);
  }
}
