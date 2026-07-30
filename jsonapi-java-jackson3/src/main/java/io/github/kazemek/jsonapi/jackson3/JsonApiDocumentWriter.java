package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.validation.JsonApiDocumentValidator;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Objects;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.json.JsonMapper;

/**
 * Validates a {@link JsonApiDocument} against a bound {@link ValidationContext}, then writes it
 * with deterministic JSON:API v1.1 wire semantics.
 *
 * <p>Aggregate validation always runs before generator output starts, so validation failure cannot
 * leave a partially written document.
 */
public final class JsonApiDocumentWriter {

  private final JsonMapper mapper;
  private final ValidationContext context;
  private final JsonApiDocumentValidator validator = new JsonApiDocumentValidator();

  JsonApiDocumentWriter(JsonMapper mapper, ValidationContext context) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.context = Objects.requireNonNull(context, "context");
  }

  /** Validation context bound to this writer. */
  public ValidationContext context() {
    return context;
  }

  /** Codec-configured mapper used for emission (derived; not the caller's original mapper). */
  JsonMapper mapper() {
    return mapper;
  }

  public String writeValueAsString(JsonApiDocument document) {
    Objects.requireNonNull(document, "document");
    validator.validate(document, context);
    return mapper.writeValueAsString(document);
  }

  public byte[] writeValueAsBytes(JsonApiDocument document) {
    Objects.requireNonNull(document, "document");
    validator.validate(document, context);
    return mapper.writeValueAsBytes(document);
  }

  public void writeValue(OutputStream out, JsonApiDocument document) {
    Objects.requireNonNull(out, "out");
    Objects.requireNonNull(document, "document");
    validator.validate(document, context);
    mapper.writeValue(out, document);
  }

  public void writeValue(Writer out, JsonApiDocument document) {
    Objects.requireNonNull(out, "out");
    Objects.requireNonNull(document, "document");
    validator.validate(document, context);
    mapper.writeValue(out, document);
  }

  public void writeValue(JsonGenerator generator, JsonApiDocument document) {
    Objects.requireNonNull(generator, "generator");
    Objects.requireNonNull(document, "document");
    validator.validate(document, context);
    mapper.writeValue(generator, document);
  }
}
