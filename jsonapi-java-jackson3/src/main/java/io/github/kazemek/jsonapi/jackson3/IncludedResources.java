package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.ResourceIdentity;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Independently bound {@code included} resources of a {@link JsonApiDomainDocument}.
 *
 * <p>{@link #resources()} preserves wire order. {@link #find(ResourceIdentity)} resolves a bound
 * DTO instance by structured identity; resources carrying both {@code id} and {@code lid} are
 * indexed under both {@link ResourceIdentity#ofId(String, String)} and {@link
 * ResourceIdentity#ofLid(String, String)} keys pointing to the same instance.
 *
 * <p>Instances are immutable: the wire-order list and the identity index are defensively copied at
 * construction, and mutating a construction-time source or a returned {@link #resources()} list
 * never changes wire order or {@link #find(ResourceIdentity)} results.
 */
public final class IncludedResources {

  private final List<Object> resources;
  private final Map<ResourceIdentity, Object> identityIndex;

  public IncludedResources(List<Object> resources, Map<ResourceIdentity, Object> identityIndex) {
    this.resources = List.copyOf(Objects.requireNonNull(resources, "resources"));
    this.identityIndex = Map.copyOf(Objects.requireNonNull(identityIndex, "identityIndex"));
  }

  /** Bound DTOs in wire order; unmodifiable. */
  public List<Object> resources() {
    return resources;
  }

  /** Returns the bound DTO indexed under the given identity, if any. */
  public Optional<Object> find(ResourceIdentity identity) {
    Objects.requireNonNull(identity, "identity");
    return Optional.ofNullable(identityIndex.get(identity));
  }
}
