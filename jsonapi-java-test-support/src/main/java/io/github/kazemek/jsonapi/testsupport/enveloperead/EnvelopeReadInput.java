package io.github.kazemek.jsonapi.testsupport.enveloperead;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import java.util.Objects;

/**
 * Discriminated envelope-read input: a Phase 2.12 codec scenario id, a named envelope-binding
 * document, or a version-neutral core {@link JsonApiDocument} whose wire form is the resolvability
 * target.
 */
public sealed interface EnvelopeReadInput
    permits EnvelopeReadInput.CodecFixture,
        EnvelopeReadInput.BindingDocument,
        EnvelopeReadInput.CoreDocument {

  /** Reference to a {@code CodecScenarios} id. */
  record CodecFixture(String codecScenarioId) implements EnvelopeReadInput {
    public CodecFixture {
      Objects.requireNonNull(codecScenarioId, "codecScenarioId");
    }
  }

  /** Named document under the shared corpus {@code envelope-binding/} directory. */
  record BindingDocument(EnvelopeBindingDocument document) implements EnvelopeReadInput {
    public BindingDocument {
      Objects.requireNonNull(document, "document");
    }
  }

  /**
   * Core document for {@code fromDocument} entry, plus the named wire form used as the
   * resolvability and documentation target.
   */
  record CoreDocument(EnvelopeBindingDocument wireForm, JsonApiDocument document)
      implements EnvelopeReadInput {
    public CoreDocument {
      Objects.requireNonNull(wireForm, "wireForm");
      Objects.requireNonNull(document, "document");
    }
  }

  static CodecFixture codec(String codecScenarioId) {
    return new CodecFixture(codecScenarioId);
  }

  static BindingDocument binding(EnvelopeBindingDocument document) {
    return new BindingDocument(document);
  }

  static CoreDocument core(EnvelopeBindingDocument wireForm, JsonApiDocument document) {
    return new CoreDocument(wireForm, document);
  }
}
