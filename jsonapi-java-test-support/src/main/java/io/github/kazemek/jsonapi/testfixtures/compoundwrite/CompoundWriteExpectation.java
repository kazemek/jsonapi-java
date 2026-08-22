package io.github.kazemek.jsonapi.testfixtures.compoundwrite;

import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Discriminated compound-write expectation: a success carrying ordered included identities (or
 * concurrent isolation of two successes), or a failure carrying a shared mapping diagnostic.
 *
 * <p>{@code included == null} is an absent {@code included} member; an empty list is a present
 * empty array. {@code propertyPath} and {@code resourceClass} are present only when the shared
 * catalog asserts them.
 */
public sealed interface CompoundWriteExpectation
    permits CompoundWriteExpectation.Success,
        CompoundWriteExpectation.Failure,
        CompoundWriteExpectation.ConcurrentIsolation {

  /**
   * Successful inclusion outcome.
   *
   * @param included {@code null} when {@code included} is omitted; empty when {@code included: []}
   * @param offPathRelationship relationship that must not be extra-read by traversal, when the
   *     model observes access; otherwise {@code null}
   * @param expectedTraversalDelta {@code 0} when {@code offPathRelationship} is set (count with
   *     include minus count without include); otherwise {@code null}
   */
  record Success(
      @Nullable List<IncludedResourceRef> included,
      @Nullable String offPathRelationship,
      @Nullable Integer expectedTraversalDelta)
      implements CompoundWriteExpectation {

    public Success {
      if ((offPathRelationship == null) != (expectedTraversalDelta == null)) {
        throw new IllegalArgumentException(
            "offPathRelationship and expectedTraversalDelta must both be present or both absent");
      }
      if (expectedTraversalDelta != null && expectedTraversalDelta != 0) {
        throw new IllegalArgumentException(
            "expectedTraversalDelta must be 0 when present: " + expectedTraversalDelta);
      }
      if (included != null) {
        included = List.copyOf(included);
      }
    }
  }

  record Failure(
      MappingDiagnostic diagnostic, @Nullable String propertyPath, @Nullable Class<?> resourceClass)
      implements CompoundWriteExpectation {
    public Failure {
      Objects.requireNonNull(diagnostic, "diagnostic");
    }
  }

  /** Two successful mappings executed concurrently against one mapper. */
  record ConcurrentIsolation(Success first, Success second) implements CompoundWriteExpectation {
    public ConcurrentIsolation {
      Objects.requireNonNull(first, "first");
      Objects.requireNonNull(second, "second");
    }
  }

  static Success omitted() {
    return new Success(null, null, null);
  }

  static Success emptyIncluded() {
    return new Success(List.of(), null, null);
  }

  static Success included(IncludedResourceRef... refs) {
    return new Success(List.of(refs), null, null);
  }

  static Success includedWithOffPathDelta(List<IncludedResourceRef> included, String offPath) {
    return new Success(included, offPath, 0);
  }

  static Failure failure(MappingDiagnostic diagnostic) {
    return new Failure(diagnostic, null, null);
  }

  static Failure failure(MappingDiagnostic diagnostic, @Nullable String propertyPath) {
    return new Failure(diagnostic, propertyPath, null);
  }

  static Failure failure(
      MappingDiagnostic diagnostic, @Nullable String propertyPath, Class<?> resourceClass) {
    return new Failure(diagnostic, propertyPath, resourceClass);
  }

  static ConcurrentIsolation concurrentIsolation(Success first, Success second) {
    return new ConcurrentIsolation(first, second);
  }
}
