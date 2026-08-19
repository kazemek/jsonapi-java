package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Typed PATCH model with an explicit {@code PatchPresence<Object>} whole-meta target. */
@JsonApiResource(type = "articles")
public record ObjectMetaPatch(@JsonApiId String id, @JsonApiMeta PatchPresence<Object> meta) {}
