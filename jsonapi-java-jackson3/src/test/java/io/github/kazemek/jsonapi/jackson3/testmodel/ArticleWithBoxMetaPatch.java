package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Typed PATCH DTO with a parameterized generic whole-meta target. */
@JsonApiResource(type = "articles")
public record ArticleWithBoxMetaPatch(
    @JsonApiId String id,
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiMeta PatchPresence<MetaBox<Integer>> meta) {}
