package io.github.kazemek.jsonapi.jackson;

import io.github.kazemek.jsonapi.core.model.ResourceIdentity;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Independently bound {@code included} resources of a domain document envelope.
 *
 * <p>{@link #resources()} preserves wire order. {@link #find(ResourceIdentity)} resolves a bound
 * DTO instance by structured identity; resources carrying both {@code id} and {@code lid} are
 * indexed under both {@link ResourceIdentity#ofId(String, String)} and {@link
 * ResourceIdentity#ofLid(String, String)} keys pointing to the same instance.
 *
 * <p>Instances are immutable: the wire-order list and the identity index are defensively copied at
 * construction, and mutating a construction-time source or a returned {@link #resources()} list
 * never changes wire order or {@link #find(ResourceIdentity)} results.
 *
 * <p>Assemble instances with {@link #of(List, Map)}. The identity index maps each identity to the
 * position of its bound DTO in the resource list, so an index entry can never point at an object
 * outside the list: inconsistent states are unrepresentable, and out-of-range positions are
 * rejected at construction. Major-specific readers build the index from validated documents, so
 * wire order and identity lookup always agree.
 */
public final class IncludedResources {

  private final List<Object> resources;
  private final Map<ResourceIdentity, Integer> identityIndex;

  private IncludedResources(List<Object> resources, Map<ResourceIdentity, Integer> identityIndex) {
    this.resources = resources;
    this.identityIndex = identityIndex;
  }

  /**
   * Creates an instance from wire-ordered bound DTOs and an identity index of {@code 0}-based
   * positions into {@code resources}.
   *
   * <p>Both arguments are defensively copied. Every index position must fall within the resource
   * list, and duplicate or {@code null} identities are rejected.
   *
   * @throws NullPointerException when {@code resources}, {@code identityIndex}, an element, an
   *     identity, or a position is {@code null}
   * @throws IllegalArgumentException when a position is negative or not less than the resource list
   *     size
   */
  public static IncludedResources of(
      List<Object> resources, Map<ResourceIdentity, Integer> identityIndex) {
    List<Object> copiedResources = List.copyOf(Objects.requireNonNull(resources, "resources"));
    Map<ResourceIdentity, Integer> copiedIndex =
        Map.copyOf(Objects.requireNonNull(identityIndex, "identityIndex"));
    for (Integer position : copiedIndex.values()) {
      Objects.requireNonNull(position, "identity index position");
      if (position < 0 || position >= copiedResources.size()) {
        throw new IllegalArgumentException(
            "Identity index position out of range [0, "
                + copiedResources.size()
                + "): "
                + position);
      }
    }
    return new IncludedResources(copiedResources, copiedIndex);
  }

  /** Bound DTOs in wire order; unmodifiable. */
  public List<Object> resources() {
    return resources;
  }

  /** Returns the bound DTO indexed under the given identity, if any. */
  public Optional<Object> find(ResourceIdentity identity) {
    Objects.requireNonNull(identity, "identity");
    Integer position = identityIndex.get(identity);
    return position == null ? Optional.empty() : Optional.of(resources.get(position));
  }
}
