package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Article with two to-one people relationships for conflicting-representation tests. */
@JsonApiResource(type = "articles")
public record ConflictArticle(
    @JsonApiId String id,
    @JsonApiRelationship Person author,
    @JsonApiRelationship Person reviewer) {}
