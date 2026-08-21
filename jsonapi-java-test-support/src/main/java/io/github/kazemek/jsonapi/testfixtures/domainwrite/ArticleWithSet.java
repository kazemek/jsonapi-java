package io.github.kazemek.jsonapi.testfixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Set;

@JsonApiResource(type = "articles")
public record ArticleWithSet(
    @JsonApiId String id, String title, @JsonApiRelationship Set<Tag> tags) {}
