package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Typed PATCH model whose whole-meta member fails during final DTO construction. */
@JsonApiResource(type = "articles")
public record ThrowingMetaPatchArticle(
    @JsonApiId String id, @JsonApiMeta PatchPresence<ThrowingMetaPatch> meta) {}
