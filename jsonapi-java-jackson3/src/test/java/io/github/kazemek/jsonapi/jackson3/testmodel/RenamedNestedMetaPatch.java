package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Typed PATCH model with a renamed nested whole-meta member. */
@JsonApiResource(type = "articles")
public record RenamedNestedMetaPatch(
    @JsonApiId String id, @JsonApiMeta PatchPresence<RenamedMetaBeanPatch> meta) {}
