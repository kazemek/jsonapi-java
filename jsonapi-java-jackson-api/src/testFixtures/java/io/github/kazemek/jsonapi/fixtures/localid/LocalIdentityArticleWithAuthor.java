package io.github.kazemek.jsonapi.fixtures.localid;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiLocalId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.fixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.fixtures.domainwrite.Person;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Passive, application-shaped carrier with independent identity roles plus ordinary relationships,
 * for create-authoring paths where the primary may carry no wire identity while its related
 * resources stay identified.
 */
@JsonApiResource(type = "articles")
public record LocalIdentityArticleWithAuthor(
    @JsonApiId @Nullable String id,
    @JsonApiLocalId @Nullable String localId,
    @JsonApiAttribute @Nullable String title,
    @JsonApiRelationship @Nullable Person author,
    @JsonApiRelationship List<Comment> comments) {}
