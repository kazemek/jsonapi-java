package io.github.kazemek.jsonapi.jackson3;

import java.util.List;
import java.util.Objects;

/**
 * Immutable mapping-only context for compound-document inclusion.
 *
 * <p>Defaults request no included resources ({@linkplain #includePaths() empty path list}) and
 * apply finite safety limits (depth 10, count 100) with {@link IncludePolicy#denyAll()}. An empty
 * path list means <em>no inclusion request</em> ({@code included} omitted). A non-empty path list
 * that resolves to zero resources emits {@code included: []}.
 *
 * <p>This context does not carry a validation context; aggregate validation remains on {@link
 * JsonApiDocumentWriter}.
 *
 * <p>{@code maxDepth == 0} rejects any non-empty include path at pre-validation with {@link
 * MappingDiagnostic#INCLUDE_DEPTH_EXCEEDED}. {@code maxIncluded == 0} fails with {@link
 * MappingDiagnostic#INCLUDE_COUNT_EXCEEDED} on the first resource that would enter {@code
 * included}.
 */
public record CompoundSerializationContext(
    List<IncludePath> includePaths, IncludePolicy includePolicy, int maxDepth, int maxIncluded) {

  private static final int DEFAULT_MAX_DEPTH = 10;
  private static final int DEFAULT_MAX_INCLUDED = 100;

  public CompoundSerializationContext {
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

  /** Empty include paths, {@link IncludePolicy#denyAll()}, depth 10, count 100. */
  public static CompoundSerializationContext defaults() {
    return new CompoundSerializationContext(
        List.of(), IncludePolicy.denyAll(), DEFAULT_MAX_DEPTH, DEFAULT_MAX_INCLUDED);
  }

  public CompoundSerializationContext withIncludePaths(List<IncludePath> paths) {
    return new CompoundSerializationContext(paths, includePolicy, maxDepth, maxIncluded);
  }

  public CompoundSerializationContext withIncludePolicy(IncludePolicy policy) {
    return new CompoundSerializationContext(includePaths, policy, maxDepth, maxIncluded);
  }

  public CompoundSerializationContext withMaxDepth(int depth) {
    return new CompoundSerializationContext(includePaths, includePolicy, depth, maxIncluded);
  }

  public CompoundSerializationContext withMaxIncluded(int count) {
    return new CompoundSerializationContext(includePaths, includePolicy, maxDepth, count);
  }
}
