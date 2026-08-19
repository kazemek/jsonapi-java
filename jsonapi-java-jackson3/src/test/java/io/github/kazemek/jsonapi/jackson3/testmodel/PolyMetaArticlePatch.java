package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Typed PATCH model with a presence-aware abstract polymorphic whole-meta target. */
@JsonApiResource(type = "articles")
public record PolyMetaArticlePatch(
    @JsonApiId String id, @JsonApiMeta PatchPresence<PolyMetaBase> meta) {}
