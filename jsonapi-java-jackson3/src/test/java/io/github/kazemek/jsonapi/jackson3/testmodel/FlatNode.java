package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;

/** Flat read-side DTO whose relationship can reference another resource of the same type. */
@JsonApiResource(type = "nodes")
public record FlatNode(@JsonApiId String id, @JsonApiRelationship ResourceIdentifier parent) {}
