package io.github.kazemek.jsonapi.testsupport.decoration;

import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Discriminated outcome for a decoration scenario. */
public sealed interface DecorationOutcome
    permits DecorationOutcome.ResourceSuccess,
        DecorationOutcome.DocumentSuccess,
        DecorationOutcome.MappedDocumentSuccess,
        DecorationOutcome.Failure {

  /** Successful resource-only decoration. */
  record ResourceSuccess(ResourceObject expected) implements DecorationOutcome {
    public ResourceSuccess {
      if (expected == null) {
        throw new IllegalArgumentException("expected must not be null");
      }
    }
  }

  /** Successful document with optional included resources (for inclusion tests). */
  record DocumentSuccess(
      ResourceObject expectedPrimary, @Nullable List<ResourceObject> expectedIncluded)
      implements DecorationOutcome {
    public DocumentSuccess {
      if (expectedPrimary == null) {
        throw new IllegalArgumentException("expectedPrimary must not be null");
      }
      if (expectedIncluded != null) {
        expectedIncluded = List.copyOf(expectedIncluded);
      }
    }
  }

  /** Successful mapped document (for sparse fieldset tests). */
  record MappedDocumentSuccess(
      ResourceObject expectedPrimary, @Nullable List<ResourceObject> expectedIncluded)
      implements DecorationOutcome {
    public MappedDocumentSuccess {
      if (expectedPrimary == null) {
        throw new IllegalArgumentException("expectedPrimary must not be null");
      }
      if (expectedIncluded != null) {
        expectedIncluded = List.copyOf(expectedIncluded);
      }
    }
  }

  /** Expected failure with stable diagnostic. */
  record Failure(MappingDiagnostic expectedDiagnostic) implements DecorationOutcome {
    public Failure {
      if (expectedDiagnostic == null) {
        throw new IllegalArgumentException("expectedDiagnostic must not be null");
      }
    }
  }
}
