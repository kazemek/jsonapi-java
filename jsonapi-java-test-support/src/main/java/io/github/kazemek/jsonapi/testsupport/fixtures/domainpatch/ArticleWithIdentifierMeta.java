package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiIdentifierMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Shared ordinary domain model with relationship meta and per-linkage identifier meta for to-one
 * author and to-many comments.
 */
@JsonApiResource(type = "articles")
public record ArticleWithIdentifierMeta(
    @JsonApiId String id,
    @JsonApiAttribute @Nullable String title,
    @JsonApiRelationship @Nullable ResourceIdentifier author,
    @JsonApiRelationship @Nullable List<ResourceIdentifier> comments,
    @JsonApiRelationshipMeta("author") @Nullable AuthorMeta authorMeta,
    @JsonApiIdentifierMeta("author") @Nullable AuthorIdMeta authorIdMeta,
    @JsonApiIdentifierMeta("comments") @Nullable List<@Nullable CommentIdMeta> commentIdMetas) {}
