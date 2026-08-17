package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO with an identifier property for negative projection scenarios. */
@JsonApiResource(type = "articles")
public record FlatArticlePatchWithId(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<String> title) {}
