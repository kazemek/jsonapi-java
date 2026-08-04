package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Flat read-side DTO with a non-String identifier coerced via convertValue. */
@JsonApiResource(type = "articles")
public record FlatIntIdArticle(@JsonApiId Integer id, String title) {}
