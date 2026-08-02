package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "articles")
public record ArticleWithOptional(
    @JsonApiId String id, String title, java.util.Optional<String> subtitle) {}
