package io.github.kazemek.jsonapi.testfixtures.enveloperead;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import org.jspecify.annotations.Nullable;

/** Flat read-side DTO whose relationship can reference another resource of the same type. */
@JsonApiResource(type = "nodes")
public record FlatNode(
    @JsonApiId String id, @JsonApiRelationship @Nullable ResourceIdentifier parent) {}
