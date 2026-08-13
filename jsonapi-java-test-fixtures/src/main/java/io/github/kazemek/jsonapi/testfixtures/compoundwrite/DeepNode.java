package io.github.kazemek.jsonapi.testfixtures.compoundwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/** Deep chain for depth-limit and nested-path tests. */
@JsonApiResource(type = "nodes")
public record DeepNode(
    @JsonApiId String id, String label, @JsonApiRelationship @Nullable DeepNode child) {}
