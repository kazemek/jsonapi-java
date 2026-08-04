package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;

/** Flat read-side DTO with an array-based to-many ResourceIdentifier relationship. */
@JsonApiResource(type = "articles")
@SuppressWarnings("ArrayRecordComponent")
public record FlatArticleWithArray(
    @JsonApiId String id, String title, @JsonApiRelationship ResourceIdentifier[] comments) {}
