package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Article whose author relationship uses a renamed JSON:API member. */
@JsonApiResource(type = "articles")
public record ArticleWithRenamedAuthor(
    @JsonApiId String id, String title, @JsonApiRelationship(name = "written-by") Person author) {}
