package io.github.kazemek.jsonapi.testfixtures.enveloperead;

import io.github.kazemek.jsonapi.core.model.ResourceIdentity;
import java.util.List;
import java.util.Objects;

/**
 * Expected independently bound {@code included} DTOs: wire order plus identity-lookup probes.
 * {@code sharedInstanceAcrossPresentProbes} requires every present probe to return the same DTO
 * instance.
 */
public record IncludedExpectation(
    List<Object> resources, List<IdentityProbe> probes, boolean sharedInstanceAcrossPresentProbes) {

  public IncludedExpectation {
    Objects.requireNonNull(resources, "resources");
    Objects.requireNonNull(probes, "probes");
    resources = List.copyOf(resources);
    probes = List.copyOf(probes);
  }

  /** One identity-index lookup: present (equals the matching DTO) or absent. */
  public record IdentityProbe(ResourceIdentity identity, boolean expectedPresent) {
    public IdentityProbe {
      Objects.requireNonNull(identity, "identity");
    }

    public static IdentityProbe present(ResourceIdentity identity) {
      return new IdentityProbe(identity, true);
    }

    public static IdentityProbe absent(ResourceIdentity identity) {
      return new IdentityProbe(identity, false);
    }
  }

  public static IncludedExpectation empty() {
    return new IncludedExpectation(List.of(), List.of(), false);
  }

  public static IncludedExpectation of(List<Object> resources, IdentityProbe... probes) {
    return new IncludedExpectation(resources, List.of(probes), false);
  }

  public static IncludedExpectation sharedInstance(
      List<Object> resources, IdentityProbe... probes) {
    return new IncludedExpectation(resources, List.of(probes), true);
  }
}
