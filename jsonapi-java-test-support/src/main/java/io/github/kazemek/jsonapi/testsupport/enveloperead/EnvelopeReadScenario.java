package io.github.kazemek.jsonapi.testsupport.enveloperead;

import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.Objects;

/**
 * One immutable typed-envelope read scenario: a stable id and exactly one {@link
 * EnvelopeReadVariant} (document-binding or registry).
 */
public record EnvelopeReadScenario(String id, EnvelopeReadVariant variant) implements Scenario {

  public EnvelopeReadScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(variant, "variant");
    if (variant instanceof EnvelopeReadVariant.DocumentBinding binding) {
      for (EnvelopeReadCase envelopeCase : binding.cases()) {
        validateCase(id, binding.entryPoint(), envelopeCase);
      }
    }
  }

  private static void validateCase(
      String id, EnvelopeEntryPoint entryPoint, EnvelopeReadCase envelopeCase) {
    EnvelopeReadInput input = envelopeCase.input();
    EnvelopeReaderContext readerContext = envelopeCase.readerContext();
    if (entryPoint == EnvelopeEntryPoint.FROM_DOCUMENT
        && !(input instanceof EnvelopeReadInput.CoreDocument)) {
      throw new IllegalArgumentException(
          "fromDocument scenarios require CoreDocument inputs: " + id);
    }
    if (entryPoint == EnvelopeEntryPoint.READ_VALUE
        && input instanceof EnvelopeReadInput.CoreDocument) {
      throw new IllegalArgumentException(
          "readValue scenarios must not carry CoreDocument inputs: " + id);
    }
    if (readerContext == EnvelopeReaderContext.CODEC_DERIVED
        && !(input instanceof EnvelopeReadInput.CodecFixture)) {
      throw new IllegalArgumentException("CODEC_DERIVED requires a codec-fixture input: " + id);
    }
  }

  @Override
  public String toString() {
    return id;
  }
}
