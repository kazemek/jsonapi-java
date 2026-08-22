package io.github.kazemek.jsonapi.testfixtures.sparsefieldset;

import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Discriminated sparse-fieldset expectation: a mapped success carrying resource states and the
 * full-linkage exception flag, an unmapped three-argument success without that flag, a concurrent
 * isolation of two mapped successes, an identity-preservation check, or a failure carrying a shared
 * mapping diagnostic.
 *
 * <p>{@code included == null} is an absent {@code included} member; an empty list is a present
 * empty array. {@code propertyPath} and {@code resourceClass} are {@code null} for {@link
 * MappingDiagnostic#FIELDSETS_REQUIRE_MAPPED_DOCUMENT} in the pinned Jackson 3 behavior.
 */
public sealed interface SparseFieldsetExpectation
    permits SparseFieldsetExpectation.MappedSuccess,
        SparseFieldsetExpectation.UnmappedSuccess,
        SparseFieldsetExpectation.Failure,
        SparseFieldsetExpectation.ConcurrentIsolation,
        SparseFieldsetExpectation.IdentityPreservation {

  /**
   * Successful {@code MappedDocument} outcome.
   *
   * @param included {@code null} when {@code included} is omitted; empty when {@code included: []}
   * @param zeroReads shared unread-getter guarantee, when the input observes access; otherwise
   *     {@code null}
   */
  record MappedSuccess(
      FieldsetResourceState primary,
      @Nullable List<FieldsetResourceState> included,
      boolean sparseFieldsetException,
      @Nullable ZeroReadGuarantee zeroReads)
      implements SparseFieldsetExpectation {

    public MappedSuccess {
      Objects.requireNonNull(primary, "primary");
      if (included != null) {
        included = List.copyOf(included);
      }
    }
  }

  /**
   * Successful three-argument {@code toDocument}/{@code toResourceCollection} outcome (empty
   * fieldset map, Phase 2.3 equivalent) without a sparse-fieldset exception flag.
   */
  record UnmappedSuccess(
      FieldsetResourceState primary, @Nullable List<FieldsetResourceState> included)
      implements SparseFieldsetExpectation {

    public UnmappedSuccess {
      Objects.requireNonNull(primary, "primary");
      if (included != null) {
        included = List.copyOf(included);
      }
    }
  }

  record Failure(
      MappingDiagnostic diagnostic, @Nullable String propertyPath, @Nullable Class<?> resourceClass)
      implements SparseFieldsetExpectation {
    public Failure {
      Objects.requireNonNull(diagnostic, "diagnostic");
    }
  }

  /** Two successful mapped mappings executed concurrently against one mapper. */
  record ConcurrentIsolation(MappedSuccess first, MappedSuccess second)
      implements SparseFieldsetExpectation {
    public ConcurrentIsolation {
      Objects.requireNonNull(first, "first");
      Objects.requireNonNull(second, "second");
    }
  }

  /** Identity ({@code type} and {@code id}) preserved under every fieldset shape. */
  record IdentityPreservation(String type, String id) implements SparseFieldsetExpectation {
    public IdentityPreservation {
      Objects.requireNonNull(type, "type");
      Objects.requireNonNull(id, "id");
    }
  }

  static MappedSuccess mapped(
      FieldsetResourceState primary,
      @Nullable List<FieldsetResourceState> included,
      boolean sparseFieldsetException) {
    return new MappedSuccess(primary, included, sparseFieldsetException, null);
  }

  static MappedSuccess mappedWithZeroReads(
      FieldsetResourceState primary,
      @Nullable List<FieldsetResourceState> included,
      boolean sparseFieldsetException,
      ZeroReadGuarantee zeroReads) {
    return new MappedSuccess(primary, included, sparseFieldsetException, zeroReads);
  }

  static UnmappedSuccess unmapped(
      FieldsetResourceState primary, @Nullable List<FieldsetResourceState> included) {
    return new UnmappedSuccess(primary, included);
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

  static ConcurrentIsolation concurrentIsolation(MappedSuccess first, MappedSuccess second) {
    return new ConcurrentIsolation(first, second);
  }

  static IdentityPreservation identity(String type, String id) {
    return new IdentityPreservation(type, id);
  }
}
