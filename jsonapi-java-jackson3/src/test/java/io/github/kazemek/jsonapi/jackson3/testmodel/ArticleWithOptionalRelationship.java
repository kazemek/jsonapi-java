package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Comment;
import java.util.Optional;

@JsonApiResource(type = "articles")
public record ArticleWithOptionalRelationship(
    @JsonApiId String id, @JsonApiRelationship Optional<Comment> comment) {}
