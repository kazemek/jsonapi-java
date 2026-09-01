package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One side of a concurrent sparse-fieldset mapping: a supplier-based single input plus the
 * representation selection and policy used for that side.
 */
public record SparseFieldsetSide(
    Supplier<Object> supplier, RepresentationSelection selection, RepresentationPolicy policy) {

  public SparseFieldsetSide {
    Objects.requireNonNull(supplier, "supplier");
    Objects.requireNonNull(selection, "selection");
    Objects.requireNonNull(policy, "policy");
  }
}
