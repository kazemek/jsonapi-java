package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainpatch.AuthorMeta;

/** Invalid declaration: relationship meta referencing an unmapped relationship. */
@JsonApiResource(type = "articles")
public record UnmappedRelationshipMetaArticle(
    @JsonApiId String id, @JsonApiRelationshipMeta("nonexistent") AuthorMeta authorMeta) {}
