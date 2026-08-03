package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Deep chain for depth-limit and nested-path tests. */
@JsonApiResource(type = "nodes")
public record DeepNode(@JsonApiId String id, String label, @JsonApiRelationship DeepNode child) {}
