package io.github.kazemek.jsonapi.testsupport.enveloperead;

/** Entry-point discriminator for document-binding envelope scenarios. */
public enum EnvelopeEntryPoint {
  /** Transport read via {@code readValue}. */
  READ_VALUE,
  /** Raw-document bind via {@code fromDocument}. */
  FROM_DOCUMENT
}
