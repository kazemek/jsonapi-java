package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Invalid typed PATCH declaration: PatchPresence wrapping a scalar meta target. */
@JsonApiResource(type = "articles")
public record ScalarMetaPatch(
    @JsonApiId String id,
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiMeta PatchPresence<String> meta) {}
