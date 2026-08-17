package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO with an implicit identifier property named {@code id}. */
@JsonApiResource(type = "articles")
public record FlatArticlePatchImplicitId(
    String id, @JsonApiAttribute PatchPresence<String> title) {}
