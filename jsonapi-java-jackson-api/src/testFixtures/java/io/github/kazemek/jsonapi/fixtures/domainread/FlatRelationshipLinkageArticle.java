package io.github.kazemek.jsonapi.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.fixtures.domainpatch.AuthorMeta;
import io.github.kazemek.jsonapi.fixtures.domainpatch.CommentIdMeta;
import io.github.kazemek.jsonapi.fixtures.domainpatch.CommentsRelationshipMeta;
import io.github.kazemek.jsonapi.jackson.mapping.RelationshipLinkage;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Flat read-side DTO with opt-in {@link RelationshipLinkage} identifier meta for to-one author and
 * to-many comments, plus independent relationship-level meta at both locations.
 */
@JsonApiResource(type = "articles")
public record FlatRelationshipLinkageArticle(
    @JsonApiId String id,
    @JsonApiAttribute @Nullable String title,
    @JsonApiRelationship @Nullable RelationshipLinkage<ResourceIdentifier, AuthorIdMeta> author,
    @JsonApiRelationship
        @Nullable List<RelationshipLinkage<ResourceIdentifier, CommentIdMeta>> comments,
    @JsonApiRelationshipMeta(relationship = "author") @Nullable AuthorMeta authorMeta,
    @JsonApiRelationshipMeta(relationship = "comments")
        @Nullable CommentsRelationshipMeta commentsMeta) {}
