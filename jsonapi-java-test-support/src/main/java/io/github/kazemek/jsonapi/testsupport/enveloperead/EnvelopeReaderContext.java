package io.github.kazemek.jsonapi.testsupport.enveloperead;

/**
 * Reader-context discriminator resolved per input. Adapters construct {@code DocumentReadContext}
 * from this discriminator and the referenced codec scenario when applicable.
 */
public enum EnvelopeReaderContext {
  /**
   * {@code DocumentReadContext.of(codecScenario.context(), codecScenario.primaryDataKind() != null
   * ? codecScenario.primaryDataKind() : PrimaryDataKind.RESOURCE)}.
   */
  CODEC_DERIVED,
  /** {@code DocumentReadContext.resourceDefaults()}. */
  RESOURCE_DEFAULTS,
  /** {@code DocumentReadContext.identifierDefaults()}. */
  IDENTIFIER_DEFAULTS
}
