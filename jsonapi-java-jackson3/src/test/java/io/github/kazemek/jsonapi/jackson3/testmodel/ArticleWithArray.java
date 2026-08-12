package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Comment;

@JsonApiResource(type = "articles")
@SuppressWarnings("ArrayRecordComponent")
public record ArticleWithArray(
    @JsonApiId String id, String title, @JsonApiRelationship Comment[] comments) {}
