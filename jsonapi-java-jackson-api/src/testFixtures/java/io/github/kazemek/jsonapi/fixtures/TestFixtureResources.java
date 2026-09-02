package io.github.kazemek.jsonapi.fixtures;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Classpath access to the shared JSON:API corpus and pinned schema resources. */
public final class TestFixtureResources {

  /** Project-authored JSON:API corpus. */
  public static final String CORPUS_ROOT = "jsonapi/corpus/1.1/";

  /** Vendored JSON:API draft schemas. */
  public static final String SCHEMA_ROOT = "jsonapi/schema/vendor/1.1-pr1603/";

  private TestFixtureResources() {}

  /** Returns a corpus resource decoded as UTF-8. */
  public static String readCorpusUtf8(String relativePath) {
    return utf8(readResource(CORPUS_ROOT + requireRelative(relativePath)));
  }

  /** Returns the exact bytes of a pinned schema resource. */
  public static byte[] readSchemaBytes(String relativePath) {
    return readResource(SCHEMA_ROOT + requireRelative(relativePath));
  }

  /** Returns a pinned schema resource decoded as UTF-8. */
  public static String readSchemaUtf8(String relativePath) {
    return utf8(readSchemaBytes(relativePath));
  }

  /** Returns whether a pinned schema resource exists. */
  public static boolean schemaExists(String relativePath) {
    return resourceUrl(SCHEMA_ROOT + requireRelative(relativePath)) != null;
  }

  private static String utf8(byte[] bytes) {
    return new String(bytes, StandardCharsets.UTF_8);
  }

  private static String requireRelative(String relativePath) {
    Objects.requireNonNull(relativePath, "relativePath");
    String normalized = relativePath.replace('\\', '/');
    if (normalized.isEmpty() || normalized.charAt(0) == '/') {
      throw new IllegalArgumentException("Invalid test-fixture resource path: " + relativePath);
    }
    for (String segment : normalized.split("/", -1)) {
      if (segment.equals("..")) {
        throw new IllegalArgumentException("Invalid test-fixture resource path: " + relativePath);
      }
    }
    return normalized;
  }

  private static byte[] readResource(String resourcePath) {
    URL url = resourceUrl(resourcePath);
    if (url == null) {
      throw new IllegalStateException("Missing test-fixture classpath resource: " + resourcePath);
    }
    try (InputStream in = url.openStream()) {
      return in.readAllBytes();
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Failed to read test-fixture classpath resource: " + resourcePath, e);
    }
  }

  private static @Nullable URL resourceUrl(String resourcePath) {
    ClassLoader loader = TestFixtureResources.class.getClassLoader();
    if (loader != null) {
      URL url = loader.getResource(resourcePath);
      if (url != null) {
        return url;
      }
    }
    return TestFixtureResources.class.getResource("/" + resourcePath);
  }
}
