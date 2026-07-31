package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.jackson3.internal.JsonApiDocumentModule;
import java.util.Objects;
import tools.jackson.databind.json.JsonMapper;

/**
 * Factory for Jackson 3 JSON:API document writers and readers.
 *
 * <p>Callers supply an existing {@link JsonMapper} or {@link JsonMapper.Builder}; this factory
 * always derives a <em>new</em> mapper via {@link JsonMapper#rebuild()} and never mutates or
 * replaces the caller's configuration in place. Public codec access is only through {@link
 * JsonApiDocumentWriter} and {@link JsonApiDocumentReader}.
 */
public final class JsonApiJackson3 {

  private JsonApiJackson3() {}

  /**
   * Returns a writer that validates with {@link ValidationContext#defaults()} then serializes
   * documents using a derived codec-configured mapper.
   */
  public static JsonApiDocumentWriter writer(JsonMapper base) {
    return writer(base, ValidationContext.defaults());
  }

  /**
   * Returns a writer that validates with the given context then serializes documents using a
   * derived codec-configured mapper.
   */
  public static JsonApiDocumentWriter writer(JsonMapper base, ValidationContext context) {
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(context, "context");
    return new JsonApiDocumentWriter(documentMapper(base), context);
  }

  /**
   * Returns a writer derived from a caller-supplied builder. The builder is not given the JSON:API
   * module; {@link JsonMapper.Builder#build()} is called, then a codec mapper is derived via {@link
   * JsonMapper#rebuild()}.
   */
  public static JsonApiDocumentWriter writer(JsonMapper.Builder base) {
    return writer(base, ValidationContext.defaults());
  }

  /**
   * Returns a writer derived from a caller-supplied builder and validation context. The builder is
   * not given the JSON:API module.
   */
  public static JsonApiDocumentWriter writer(JsonMapper.Builder base, ValidationContext context) {
    Objects.requireNonNull(base, "base");
    return writer(base.build(), context);
  }

  /**
   * Returns a reader bound to the given read context. Decoding is token-driven and does not use
   * document serializers, so the caller mapper is used as-is.
   */
  public static JsonApiDocumentReader reader(JsonMapper base, DocumentReadContext context) {
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(context, "context");
    return new JsonApiDocumentReader(base, context);
  }

  /**
   * Returns a reader derived from a caller-supplied builder and read context. The builder is not
   * given the JSON:API module.
   */
  public static JsonApiDocumentReader reader(JsonMapper.Builder base, DocumentReadContext context) {
    Objects.requireNonNull(base, "base");
    return reader(base.build(), context);
  }

  /**
   * Derives a new mapper with JSON:API document serializers registered. Package-private so callers
   * cannot serialize documents without aggregate validation.
   */
  static JsonMapper documentMapper(JsonMapper base) {
    Objects.requireNonNull(base, "base");
    return base.rebuild().addModule(new JsonApiDocumentModule()).build();
  }
}
