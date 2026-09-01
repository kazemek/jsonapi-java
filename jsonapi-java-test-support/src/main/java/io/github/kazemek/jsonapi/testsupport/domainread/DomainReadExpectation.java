package io.github.kazemek.jsonapi.testsupport.domainread;

import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Discriminated binder expectation: a complete bound DTO value (or collection of values) or a
 * shared mapping diagnostic. {@code propertyPath} and {@code resourceClass} are present only when
 * the shared catalog asserts them; Jackson-derived property-name paths and major-specific cause
 * types stay in adapter-local supplementary assertions.
 */
public sealed interface DomainReadExpectation
    permits DomainReadExpectation.BoundValue, DomainReadExpectation.Failure {

  record BoundValue(Object value) implements DomainReadExpectation {
    public BoundValue {
      Objects.requireNonNull(value, "value");
    }
  }

  record Failure(
      MappingDiagnostic diagnostic, @Nullable String propertyPath, @Nullable Class<?> resourceClass)
      implements DomainReadExpectation {
    public Failure {
      Objects.requireNonNull(diagnostic, "diagnostic");
    }
  }

  static BoundValue bound(Object value) {
    return new BoundValue(value);
  }

  static Failure failure(MappingDiagnostic diagnostic) {
    return new Failure(diagnostic, null, null);
  }

  static Failure failure(MappingDiagnostic diagnostic, String propertyPath) {
    return new Failure(diagnostic, propertyPath, null);
  }

  static Failure failure(
      MappingDiagnostic diagnostic, String propertyPath, Class<?> resourceClass) {
    return new Failure(diagnostic, propertyPath, resourceClass);
  }
}
