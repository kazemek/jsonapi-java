package io.github.kazemek.jsonapi.testfixtures.sparsefieldset;

import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Typed sparse-fieldset request: a single or collection mapping with a serialization context, two
 * concurrent mappings, or the four identity-preservation fieldset shapes. Suppliers defer input
 * construction until the adapter consumer invokes the scenario, so fresh domain instances are
 * mapped on each run.
 */
public sealed interface SparseFieldsetRequest
    permits SparseFieldsetRequest.Single,
        SparseFieldsetRequest.Collection,
        SparseFieldsetRequest.Concurrent,
        SparseFieldsetRequest.IdentityPreservation {

  /** Single primary with an explicit {@code CompoundSerializationContext}. */
  record Single(Supplier<@Nullable Object> supplier, CompoundSerializationContext context)
      implements SparseFieldsetRequest {
    public Single {
      requireSupplier(supplier);
      Objects.requireNonNull(context, "context");
    }
  }

  /** Collection primary with an explicit {@code CompoundSerializationContext}. */
  record Collection(Supplier<Iterable<?>> supplier, CompoundSerializationContext context)
      implements SparseFieldsetRequest {
    public Collection {
      requireSupplier(supplier);
      Objects.requireNonNull(context, "context");
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
  record IdentityPreservation(
      Supplier<Object> supplier, List<CompoundSerializationContext> contexts)
      implements SparseFieldsetRequest {
    public static final int SHAPE_COUNT = 4;

    public IdentityPreservation {
      requireSupplier(supplier);
      Objects.requireNonNull(contexts, "contexts");
      contexts = List.copyOf(contexts);
      if (contexts.size() != SHAPE_COUNT) {
        throw new IllegalArgumentException(
            "Identity preservation requires "
                + SHAPE_COUNT
                + " fieldset shapes: "
                + contexts.size());
      }
    }
  }

  static Single single(Supplier<@Nullable Object> supplier, CompoundSerializationContext context) {
    return new Single(supplier, context);
  }

  static Collection collection(
      Supplier<Iterable<?>> supplier, CompoundSerializationContext context) {
    return new Collection(supplier, context);
  }

  static Concurrent concurrent(SparseFieldsetSide first, SparseFieldsetSide second) {
    return new Concurrent(first, second);
  }

  static IdentityPreservation identityPreservation(
      Supplier<Object> supplier, List<CompoundSerializationContext> contexts) {
    return new IdentityPreservation(supplier, contexts);
  }

  private static void requireSupplier(Supplier<?> supplier) {
    Objects.requireNonNull(supplier, "supplier");
  }
}
