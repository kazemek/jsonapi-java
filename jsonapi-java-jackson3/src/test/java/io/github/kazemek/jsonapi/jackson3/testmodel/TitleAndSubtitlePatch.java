package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO with a command-mapped title plus an extra unmapped subtitle member. */
@JsonApiResource(type = "articles")
public record TitleAndSubtitlePatch(
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiAttribute PatchPresence<String> subtitle) {}
