package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/** Flat read-side DTO with a non-String identifier coerced via convertValue. */
@JsonApiResource(type = "articles")
public record FlatIntIdArticle(@JsonApiId @Nullable Integer id, @Nullable String title) {}
