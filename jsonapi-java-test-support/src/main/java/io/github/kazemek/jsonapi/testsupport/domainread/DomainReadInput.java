package io.github.kazemek.jsonapi.testsupport.domainread;

import io.github.kazemek.jsonapi.core.model.ResourceObject;
import java.util.List;
import java.util.Objects;

/**
 * Discriminated binder input: one resource ({@code fromResource}), a homogeneous collection ({@code
 * fromResources}), or a dual-document included-isolation pair. Adapter suites dispatch on the
 * variant kind, never on a scenario id.
 */
public sealed interface DomainReadInput
    permits DomainReadInput.SingleResource,
        DomainReadInput.ResourceCollection,
        DomainReadInput.IncludedIsolation {

  record SingleResource(ResourceObject resource) implements DomainReadInput {
    public SingleResource {
      Objects.requireNonNull(resource, "resource");
    }
  }

  record ResourceCollection(List<ResourceObject> resources) implements DomainReadInput {
    public ResourceCollection {
      Objects.requireNonNull(resources, "resources");
      resources = List.copyOf(resources);
    }
  }

  /**
   * Two wire documents with identical primary data and differing {@code included} members. The
   * adapter parses both through its own reader; the binder must produce the same bound value.
   */
  record IncludedIsolation(String primaryJson, String swappedIncludedJson)
      implements DomainReadInput {
    public IncludedIsolation {
      Objects.requireNonNull(primaryJson, "primaryJson");
      Objects.requireNonNull(swappedIncludedJson, "swappedIncludedJson");
    }
  }

  static SingleResource single(ResourceObject resource) {
    return new SingleResource(resource);
  }

  static ResourceCollection collection(List<ResourceObject> resources) {
    return new ResourceCollection(resources);
  }

  static IncludedIsolation includedIsolation(String primaryJson, String swappedIncludedJson) {
    return new IncludedIsolation(primaryJson, swappedIncludedJson);
  }
}
