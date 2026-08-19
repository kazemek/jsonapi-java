package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;

/** Invalid declaration: two relationship meta properties targeting the same relationship. */
@JsonApiResource(type = "articles")
public record DuplicateRelationshipMetaArticle(
    @JsonApiId String id,
    @JsonApiRelationship ResourceIdentifier author,
    @JsonApiRelationshipMeta("author") String authorMeta,
    @JsonApiRelationshipMeta("author") String authorMeta2) {}
