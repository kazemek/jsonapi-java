package io.github.kazemek.jsonapi.testsupport.enveloperead;

import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Discriminated envelope-read variant: a document-binding scenario (entry point, reader context per
 * input, and per-input expectations) or a registry-level registration attempt.
 */
public sealed interface EnvelopeReadVariant
    permits EnvelopeReadVariant.DocumentBinding, EnvelopeReadVariant.Registry {

  /**
   * Wire-read or raw-document bind with registered target DTO classes. {@code entryPoint} is
   * required; each case carries its own {@code readerContext}.
   */
  record DocumentBinding(
      List<Class<?>> targetClasses, EnvelopeEntryPoint entryPoint, List<EnvelopeReadCase> cases)
      implements EnvelopeReadVariant {

    public DocumentBinding {
      Objects.requireNonNull(targetClasses, "targetClasses");
      Objects.requireNonNull(entryPoint, "entryPoint");
      Objects.requireNonNull(cases, "cases");
      targetClasses = List.copyOf(targetClasses);
      cases = List.copyOf(cases);
      if (cases.isEmpty()) {
        throw new IllegalArgumentException("document-binding scenarios require at least one case");
      }
    }
  }

  /**
   * Registry construction without input documents, entry-point discriminator, or reader-context
   * discriminator. Each attempt registers its target classes together and expects one diagnostic.
   */
  record Registry(List<RegistryAttempt> attempts) implements EnvelopeReadVariant {

    public Registry {
      Objects.requireNonNull(attempts, "attempts");
      attempts = List.copyOf(attempts);
      if (attempts.isEmpty()) {
        throw new IllegalArgumentException("registry scenarios require at least one attempt");
      }
    }
  }

  /** One registry {@code register}/{@code build} attempt and its expected diagnostic. */
  record RegistryAttempt(
      List<Class<?>> targetClasses,
      MappingDiagnostic diagnostic,
      @Nullable String propertyPath,
      Class<?> resourceClass) {

    public RegistryAttempt {
      Objects.requireNonNull(targetClasses, "targetClasses");
      Objects.requireNonNull(diagnostic, "diagnostic");
      Objects.requireNonNull(resourceClass, "resourceClass");
      targetClasses = List.copyOf(targetClasses);
      if (targetClasses.isEmpty()) {
        throw new IllegalArgumentException("registry attempt requires target classes");
      }
    }
  }
}
