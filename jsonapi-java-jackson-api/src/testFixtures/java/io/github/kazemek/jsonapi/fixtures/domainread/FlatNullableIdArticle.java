package io.github.kazemek.jsonapi.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/**
 * Flat read-side DTO whose nullable id role may stay unbound when no wire {@code id} is present.
 */
@JsonApiResource(type = "articles")
public record FlatNullableIdArticle(
    @JsonApiId @Nullable String id, @JsonApiAttribute @Nullable String title) {}
