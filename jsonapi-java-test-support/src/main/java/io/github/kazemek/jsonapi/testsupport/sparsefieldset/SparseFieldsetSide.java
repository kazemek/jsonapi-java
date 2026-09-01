package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

import io.github.kazemek.jsonapi.jackson.representation.CompoundSerializationContext;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One side of a concurrent sparse-fieldset mapping: a supplier-based single input plus the
 * serialization context used for that side.
 */
public record SparseFieldsetSide(Supplier<Object> supplier, CompoundSerializationContext context) {

  public SparseFieldsetSide {
    Objects.requireNonNull(supplier, "supplier");
    Objects.requireNonNull(context, "context");
  }
}
