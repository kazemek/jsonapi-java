package io.github.kazemek.jsonapi.jackson.api;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.document.DocumentReadContext;
import io.github.kazemek.jsonapi.jackson.mapping.MappedDocument;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Level-1 raw/general JSON:API document operations.
 *
 * <p>Raw document operations stay explicit where inference is unsafe: callers supply the semantic
 * {@link DocumentReadContext} (including the primary-data kind) rather than relying on the facade
 * to guess ambiguous document shapes. Writes validate before emission; {@link MappedDocument}
 * writes additionally compose sparse-fieldset linkage provenance into validation.
 */
public interface JsonApiDocuments {

  /** Decodes and validates one document under the given read context. */
  JsonApiDocument read(String json, DocumentReadContext context);

  /** Stream variant of {@link #read(String, DocumentReadContext)}. The stream is not closed. */
  JsonApiDocument read(InputStream json, DocumentReadContext context);

  /** Validates a document, then returns its JSON. */
  String write(JsonApiDocument document);

  /** Validates a document, then writes it to the given stream. The stream is not closed. */
  void write(JsonApiDocument document, OutputStream out);

  /**
   * Validates a mapped document against the bound context composed with its sparse-fieldset linkage
   * provenance, then returns its JSON.
   */
  String write(MappedDocument mapped);

  /**
   * Validates a mapped document against the bound context composed with its sparse-fieldset linkage
   * provenance, then writes it to the given stream. The stream is not closed.
   */
  void write(MappedDocument mapped, OutputStream out);
}
