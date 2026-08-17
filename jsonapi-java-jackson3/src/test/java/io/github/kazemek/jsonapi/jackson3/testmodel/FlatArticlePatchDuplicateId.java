package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO with duplicate identifier properties for negative projection scenarios. */
@JsonApiResource(type = "articles")
public record FlatArticlePatchDuplicateId(
    @JsonApiId String firstId,
    @JsonApiId String secondId,
    @JsonApiAttribute PatchPresence<String> title) {}
