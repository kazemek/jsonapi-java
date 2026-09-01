package io.github.kazemek.jsonapi.testsupport.compoundwrite;

import io.github.kazemek.jsonapi.jackson.representation.IncludePath;
import io.github.kazemek.jsonapi.jackson.representation.IncludePolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One side of a concurrent compound-write mapping: a supplier-based single input plus the neutral
 * representation selection and policy for that side.
 */
public record CompoundWriteSide(
    Supplier<Object> supplier, RepresentationSelection selection, RepresentationPolicy policy) {

  public CompoundWriteSide {
    Objects.requireNonNull(supplier, "supplier");
    Objects.requireNonNull(selection, "selection");
    Objects.requireNonNull(policy, "policy");
  }

  public CompoundWriteSide(
      Supplier<Object> supplier,
      List<IncludePath> includePaths,
      IncludePolicy includePolicy,
      int maxDepth,
      int maxIncluded) {
    this(
        supplier,
        CompoundWriteRequest.selection(includePaths),
        CompoundWriteRequest.policy(includePolicy, maxDepth, maxIncluded));
  }
}
