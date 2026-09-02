package io.github.kazemek.jsonapi.fixtures.compoundwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/** Self-referential article for primary-as-related inclusion tests. */
@JsonApiResource(type = "articles")
public record LinkedArticle(
    @JsonApiId String id, @JsonApiRelationship @Nullable LinkedArticle related) {}
