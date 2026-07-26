package io.github.kazemek.jsonapi.core.model;

import java.util.List;
import java.util.Objects;

/** Sealed primary data preserving explicit null, single, and collection states. */
public sealed interface DocumentData
    permits DocumentData.NullData,
        DocumentData.SingleResource,
        DocumentData.ResourceCollection,
        DocumentData.SingleIdentifier,
        DocumentData.IdentifierCollection {

  record NullData() implements DocumentData {
    public static final NullData INSTANCE = new NullData();
  }

  record SingleResource(ResourceObject resource) implements DocumentData {
    public SingleResource {
      Objects.requireNonNull(resource, "resource");
    }
  }

  record ResourceCollection(List<ResourceObject> resources) implements DocumentData {
    public ResourceCollection {
      resources = resources == null ? List.of() : List.copyOf(resources);
    }
  }

  record SingleIdentifier(ResourceIdentifier identifier) implements DocumentData {
    public SingleIdentifier {
      Objects.requireNonNull(identifier, "identifier");
    }
  }

  record IdentifierCollection(List<ResourceIdentifier> identifiers) implements DocumentData {
    public IdentifierCollection {
      identifiers = identifiers == null ? List.of() : List.copyOf(identifiers);
    }
  }
}
