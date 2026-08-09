package io.github.kazemek.jsonapi.jackson3;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable mapping-only context for compound-document inclusion and sparse fieldsets.
 *
 * <p>Defaults request no included resources ({@linkplain #includePaths() empty path list}), apply
 * finite safety limits (depth 10, count 100) with {@link IncludePolicy#denyAll()}, and request no
 * fieldsets ({@linkplain #fieldsets() empty map}) with {@link FieldPolicy#allowAll()}. An empty
 * path list means <em>no inclusion request</em> ({@code included} omitted). A non-empty path list
 * that resolves to zero resources emits {@code included: []}. An empty fieldset map means
 * unrestricted mapped fields; a present empty list for a type means identity-only emission.
 *
 * <p>This context does not carry a validation context; aggregate validation remains on {@link
 * JsonApiDocumentWriter}. Callers that apply fieldsets must use the {@link MappedDocument} mapper
 * overloads and pass {@link
 * MappedDocument#applyTo(io.github.kazemek.jsonapi.core.validation.ValidationContext)} into the
 * writer factory.
 *
 * <p>{@code maxDepth == 0} rejects any non-empty include path at pre-validation with {@link
 * MappingDiagnostic#INCLUDE_DEPTH_EXCEEDED}. {@code maxIncluded == 0} fails with {@link
 * MappingDiagnostic#INCLUDE_COUNT_EXCEEDED} on the first resource that would enter {@code
 * included}.
 */
public record CompoundSerializationContext(
    List<IncludePath> includePaths,
    IncludePolicy includePolicy,
    int maxDepth,
    int maxIncluded,
    Map<String, List<String>> fieldsets,
    FieldPolicy fieldPolicy) {

  private static final int DEFAULT_MAX_DEPTH = 10;
  private static final int DEFAULT_MAX_INCLUDED = 100;

  public CompoundSerializationContext {
    Objects.requireNonNull(includePaths, "includePaths");
    Objects.requireNonNull(includePolicy, "includePolicy");
    Objects.requireNonNull(fieldsets, "fieldsets");
    Objects.requireNonNull(fieldPolicy, "fieldPolicy");
    if (maxDepth < 0) {
      throw new IllegalArgumentException("maxDepth must not be negative: " + maxDepth);
    }
    if (maxIncluded < 0) {
      throw new IllegalArgumentException("maxIncluded must not be negative: " + maxIncluded);
    }
    includePaths = List.copyOf(includePaths);
    fieldsets = copyFieldsets(fieldsets);
  }

  /**
   * Empty include paths, {@link IncludePolicy#denyAll()}, depth 10, count 100, empty fieldsets,
   * {@link FieldPolicy#allowAll()}.
   */
  public static CompoundSerializationContext defaults() {
    return new CompoundSerializationContext(
        List.of(),
        IncludePolicy.denyAll(),
        DEFAULT_MAX_DEPTH,
        DEFAULT_MAX_INCLUDED,
        Map.of(),
        FieldPolicy.allowAll());
  }

  public CompoundSerializationContext withIncludePaths(List<IncludePath> paths) {
    return new CompoundSerializationContext(
        paths, includePolicy, maxDepth, maxIncluded, fieldsets, fieldPolicy);
  }

  public CompoundSerializationContext withIncludePolicy(IncludePolicy policy) {
    return new CompoundSerializationContext(
        includePaths, policy, maxDepth, maxIncluded, fieldsets, fieldPolicy);
  }

  public CompoundSerializationContext withMaxDepth(int depth) {
    return new CompoundSerializationContext(
        includePaths, includePolicy, depth, maxIncluded, fieldsets, fieldPolicy);
  }

  public CompoundSerializationContext withMaxIncluded(int count) {
    return new CompoundSerializationContext(
        includePaths, includePolicy, maxDepth, count, fieldsets, fieldPolicy);
  }

  public CompoundSerializationContext withFieldsets(Map<String, List<String>> fieldsets) {
    return new CompoundSerializationContext(
        includePaths, includePolicy, maxDepth, maxIncluded, fieldsets, fieldPolicy);
  }

  public CompoundSerializationContext withFieldPolicy(FieldPolicy policy) {
    return new CompoundSerializationContext(
        includePaths, includePolicy, maxDepth, maxIncluded, fieldsets, policy);
  }

  private static Map<String, List<String>> copyFieldsets(Map<String, List<String>> fieldsets) {
    Map<String, List<String>> copy = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : fieldsets.entrySet()) {
      String resourceType = Objects.requireNonNull(entry.getKey(), "fieldset resource type");
      List<String> fields = Objects.requireNonNull(entry.getValue(), "fieldset fields");
      LinkedHashSet<String> unique = new LinkedHashSet<>();
      for (String fieldName : fields) {
        Objects.requireNonNull(fieldName, "fieldset field name");
        unique.add(fieldName);
      }
      copy.put(resourceType, List.copyOf(unique));
    }
    return Map.copyOf(copy);
  }
}
