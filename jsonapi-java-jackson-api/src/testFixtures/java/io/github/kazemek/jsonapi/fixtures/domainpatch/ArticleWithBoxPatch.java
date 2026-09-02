package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Shared direct typed PATCH DTO with a generic nested {@link BoxPatch}<Integer> member. */
@JsonApiResource(type = "articles")
public record ArticleWithBoxPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<BoxPatch<Integer>> box) {}
