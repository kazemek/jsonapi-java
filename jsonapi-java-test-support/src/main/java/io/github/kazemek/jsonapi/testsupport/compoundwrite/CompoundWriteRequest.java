package io.github.kazemek.jsonapi.testsupport.compoundwrite;

import io.github.kazemek.jsonapi.jackson.representation.IncludePath;
import io.github.kazemek.jsonapi.jackson.representation.IncludePolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy;
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

/**
 * Typed compound-write request: a context-free single mapping, a single or collection mapping with
 * an inclusion context, or two concurrent mappings. Suppliers defer input construction until the
 * adapter consumer invokes the scenario, so fresh domain instances (and a fresh one-shot iterable)
 * are mapped on each run.
 */
public sealed interface CompoundWriteRequest
    permits CompoundWriteRequest.ContextFree,
        CompoundWriteRequest.Document,
        CompoundWriteRequest.Collection,
        CompoundWriteRequest.Concurrent {

  /** Context-free {@code toDocument} overload: no inclusion request. */
  record ContextFree(Supplier<@Nullable Object> supplier) implements CompoundWriteRequest {
    public ContextFree {
      requireSupplier(supplier);
    }
  }

  /** Single primary with explicit neutral representation selection and policy. */
  record Document(
      Supplier<@Nullable Object> supplier,
      RepresentationSelection selection,
      RepresentationPolicy policy)
      implements CompoundWriteRequest {
    public Document {
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
      implements CompoundWriteRequest {
    public Collection {
      requireSupplier(supplier);
      Objects.requireNonNull(selection, "selection");
      Objects.requireNonNull(policy, "policy");
    }
  }

  /** Two single mappings executed concurrently against one mapper. */
  record Concurrent(CompoundWriteSide first, CompoundWriteSide second)
      implements CompoundWriteRequest {
    public Concurrent {
      Objects.requireNonNull(first, "first");
      Objects.requireNonNull(second, "second");
    }
  }

  static ContextFree contextFree(Supplier<@Nullable Object> supplier) {
    return new ContextFree(supplier);
  }

  static Document document(
      Supplier<@Nullable Object> supplier,
      List<IncludePath> includePaths,
      IncludePolicy includePolicy,
      int maxDepth,
      int maxIncluded) {
    return new Document(
        supplier, selection(includePaths), policy(includePolicy, maxDepth, maxIncluded));
  }

  static Collection collection(
      Supplier<Iterable<?>> supplier,
      List<IncludePath> includePaths,
      IncludePolicy includePolicy,
      int maxDepth,
      int maxIncluded) {
    return new Collection(
        supplier, selection(includePaths), policy(includePolicy, maxDepth, maxIncluded));
  }

  static Concurrent concurrent(CompoundWriteSide first, CompoundWriteSide second) {
    return new Concurrent(first, second);
  }

  private static void requireSupplier(Supplier<?> supplier) {
    Objects.requireNonNull(supplier, "supplier");
  }

  static RepresentationSelection selection(List<IncludePath> includePaths) {
    Objects.requireNonNull(includePaths, "includePaths");
    RepresentationSelection.Builder builder = RepresentationSelection.builder();
    for (IncludePath includePath : includePaths) {
      builder.include(includePath);
    }
    return builder.build();
  }

  static RepresentationPolicy policy(IncludePolicy includePolicy, int maxDepth, int maxIncluded) {
    return RepresentationPolicy.defaults()
        .withIncludePolicy(includePolicy)
        .withMaxIncludeDepth(maxDepth)
        .withMaxIncludedResources(maxIncluded);
  }
}
