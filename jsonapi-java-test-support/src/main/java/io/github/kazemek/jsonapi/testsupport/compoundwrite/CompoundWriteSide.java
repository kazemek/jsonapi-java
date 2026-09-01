package io.github.kazemek.jsonapi.testsupport.compoundwrite;

import io.github.kazemek.jsonapi.jackson.representation.IncludePath;
import io.github.kazemek.jsonapi.jackson.representation.IncludePolicy;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * One side of a concurrent compound-write mapping: a supplier-based single input plus the
 * inclusion-context pins used to reconstruct {@code CompoundSerializationContext}.
 */
public record CompoundWriteSide(
    Supplier<Object> supplier,
    List<IncludePath> includePaths,
    IncludePolicy includePolicy,
    int maxDepth,
    int maxIncluded) {

  public CompoundWriteSide {
    Objects.requireNonNull(supplier, "supplier");
    Objects.requireNonNull(includePaths, "includePaths");
    Objects.requireNonNull(includePolicy, "includePolicy");
    if (maxDepth < 0) {
      throw new IllegalArgumentException("maxDepth must not be negative: " + maxDepth);
    }
    if (maxIncluded < 0) {
      throw new IllegalArgumentException("maxIncluded must not be negative: " + maxIncluded);
    }
    includePaths = List.copyOf(includePaths);
  }
}
