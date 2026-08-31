package io.github.kazemek.jsonapi.annotation.fixtures;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "articles")
public record AnnotatedArticleRecord(
    @JsonApiId String id,
    @JsonApiAttribute String title,
    @JsonApiAttribute String body,
    @JsonApiRelationship String authorId,
    @JsonApiRelationship String comments) {}
