package io.github.kazemek.jsonapi.core.model;

import java.util.List;
import java.util.Objects;

/** Sealed relationship linkage preserving explicit null, single, and collection states. */
public sealed interface RelationshipData
    permits RelationshipData.NullLinkage,
        RelationshipData.SingleLinkage,
        RelationshipData.IdentifierCollectionLinkage {

  record NullLinkage() implements RelationshipData {
    public static final NullLinkage INSTANCE = new NullLinkage();
  }

  record SingleLinkage(ResourceIdentifier identifier) implements RelationshipData {
    public SingleLinkage {
      Objects.requireNonNull(identifier, "identifier");
    }
  }

  record IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers)
      implements RelationshipData {
    public IdentifierCollectionLinkage {
      identifiers = identifiers == null ? List.of() : List.copyOf(identifiers);
    }

    public static IdentifierCollectionLinkage empty() {
      return new IdentifierCollectionLinkage(List.of());
    }
  }
}
