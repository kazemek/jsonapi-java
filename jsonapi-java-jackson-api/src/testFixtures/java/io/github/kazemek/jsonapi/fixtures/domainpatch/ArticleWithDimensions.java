package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Shared low-level PATCH DTO with an ordinary structured {@link Dimensions} attribute. */
@JsonApiResource(type = "articles")
public record ArticleWithDimensions(
    @JsonApiId String id, @JsonApiAttribute Dimensions dimensions) {}
