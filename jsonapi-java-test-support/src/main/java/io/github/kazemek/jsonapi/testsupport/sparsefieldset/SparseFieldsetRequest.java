package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Typed sparse-fieldset request: a single or collection mapping with representation selection and
 * policy, two concurrent mappings, or the four identity-preservation fieldset shapes. Suppliers
 * defer input construction until the adapter consumer invokes the scenario, so fresh domain
 * instances are mapped on each run.
 */
public sealed interface SparseFieldsetRequest
    permits SparseFieldsetRequest.Single,
        SparseFieldsetRequest.Collection,
        SparseFieldsetRequest.Concurrent,
        SparseFieldsetRequest.IdentityPreservation {

  /** Single primary with explicit neutral representation selection and policy. */
  record Single(
      Supplier<@Nullable Object> supplier,
      RepresentationSelection selection,
      RepresentationPolicy policy)
      implements SparseFieldsetRequest {
    public Single {
      requireSupplier(supplier);
      Objects.requireNonNull(selection, "selection");
      Objects.requireNonNull(policy, "policy");
    }
  }

  /** Collection primary with explicit neutral representation selection and policy. */
  record Collection(
      Supplier<Iterable<?>> supplier,
      RepresentationSelection selection,
      RepresentationPolicy policy)
      implements SparseFieldsetRequest {
    public Collection {
      requireSupplier(supplier);
      Objects.requireNonNull(selection, "selection");
      Objects.requireNonNull(policy, "policy");
    }
  }

  /** Two single mappings executed concurrently against one mapper. */
  record Concurrent(SparseFieldsetSide first, SparseFieldsetSide second)
      implements SparseFieldsetRequest {
    public Concurrent {
      Objects.requireNonNull(first, "first");
      Objects.requireNonNull(second, "second");
    }
  }

  /**
   * One identity-only expected state applied to the four fieldset shapes: empty map, present-empty
   * list, attribute-only, and relationship-only.
   */
  record IdentityPreservation(Supplier<Object> supplier, List<SparseFieldsetSide> sides)
      implements SparseFieldsetRequest {
    public static final int SHAPE_COUNT = 4;

    public IdentityPreservation {
      requireSupplier(supplier);
      Objects.requireNonNull(sides, "sides");
      sides = List.copyOf(sides);
      if (sides.size() != SHAPE_COUNT) {
        throw new IllegalArgumentException(
            "Identity preservation requires " + SHAPE_COUNT + " fieldset shapes: " + sides.size());
      }
    }
  }

  static Single single(
      Supplier<@Nullable Object> supplier,
      RepresentationSelection selection,
      RepresentationPolicy policy) {
    return new Single(supplier, selection, policy);
  }

  static Collection collection(
      Supplier<Iterable<?>> supplier,
      RepresentationSelection selection,
      RepresentationPolicy policy) {
    return new Collection(supplier, selection, policy);
  }

  static Concurrent concurrent(SparseFieldsetSide first, SparseFieldsetSide second) {
    return new Concurrent(first, second);
  }

  static IdentityPreservation identityPreservation(
      Supplier<Object> supplier, List<SparseFieldsetSide> sides) {
    return new IdentityPreservation(supplier, sides);
  }

  private static void requireSupplier(Supplier<?> supplier) {
    Objects.requireNonNull(supplier, "supplier");
  }
}
