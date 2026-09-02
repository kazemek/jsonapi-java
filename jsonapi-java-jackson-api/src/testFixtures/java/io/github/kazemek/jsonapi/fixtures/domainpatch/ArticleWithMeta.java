package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import org.jspecify.annotations.Nullable;

/** Shared ordinary domain model with whole-object resource and relationship meta. */
@JsonApiResource(type = "articles")
public record ArticleWithMeta(
    @JsonApiId String id,
    @JsonApiAttribute @Nullable String title,
    @JsonApiRelationship @Nullable ResourceIdentifier author,
    @JsonApiMeta @Nullable ArticleMeta meta,
    @JsonApiRelationshipMeta(relationship = "author") @Nullable AuthorMeta authorMeta) {}
