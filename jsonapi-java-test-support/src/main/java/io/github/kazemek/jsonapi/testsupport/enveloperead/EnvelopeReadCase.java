package io.github.kazemek.jsonapi.testsupport.enveloperead;

import java.util.Objects;

/**
 * One document-binding input: the document source, the reader-context discriminator, and the
 * per-input expectation.
 */
public record EnvelopeReadCase(
    EnvelopeReadInput input,
    EnvelopeReaderContext readerContext,
    EnvelopeReadExpectation expectation) {

  public EnvelopeReadCase {
    Objects.requireNonNull(input, "input");
    Objects.requireNonNull(readerContext, "readerContext");
    Objects.requireNonNull(expectation, "expectation");
  }
}
