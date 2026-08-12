package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import org.jspecify.annotations.Nullable;

/** Flat read-side DTO with an array-based to-many ResourceIdentifier relationship. */
@JsonApiResource(type = "articles")
@SuppressWarnings("ArrayRecordComponent")
public record FlatArticleWithArray(
    @JsonApiId String id,
    @Nullable String title,
    @JsonApiRelationship ResourceIdentifier @Nullable [] comments) {}
