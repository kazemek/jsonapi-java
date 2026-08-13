package io.github.kazemek.jsonapi.testfixtures.compoundwrite;

import java.util.Objects;

/**
 * First-discovery identity of one included resource: JSON:API {@code type} and {@code id} in
 * emission order.
 */
public record IncludedResourceRef(String type, String id) {

  public IncludedResourceRef {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(id, "id");
  }

  public static IncludedResourceRef of(String type, String id) {
    return new IncludedResourceRef(type, id);
  }
}
