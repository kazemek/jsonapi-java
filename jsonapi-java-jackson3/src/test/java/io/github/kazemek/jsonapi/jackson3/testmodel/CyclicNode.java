package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Mutable node graph for cyclic inclusion traversal tests. */
@JsonApiResource(type = "nodes")
public final class CyclicNode {

  private final String id;
  private final String label;
  private CyclicNode child;

  public CyclicNode(String id, String label) {
    this.id = id;
    this.label = label;
  }

  @JsonApiId
  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  @JsonApiRelationship
  public CyclicNode getChild() {
    return child;
  }

  public void setChild(CyclicNode child) {
    this.child = child;
  }
}
