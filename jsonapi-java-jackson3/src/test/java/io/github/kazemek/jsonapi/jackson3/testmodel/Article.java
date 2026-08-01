package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;

@JsonApiResource(type = "articles")
public record Article(
    @JsonApiId String id,
    @JsonApiAttribute String title,
    @JsonApiAttribute(name = "body-text") String body,
    @JsonApiRelationship List<Comment> comments,
    @JsonApiRelationship Person author) {}
