package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/** Flat read-side DTO with a non-String identifier coerced via convertValue. */
@JsonApiResource(type = "articles")
public record FlatIntIdArticle(
    @JsonApiId @Nullable Integer id, @JsonApiAttribute @Nullable String title) {}
