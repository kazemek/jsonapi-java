package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Self-referential article for primary-as-related inclusion tests. */
@JsonApiResource(type = "articles")
public record LinkedArticle(@JsonApiId String id, @JsonApiRelationship LinkedArticle related) {}
