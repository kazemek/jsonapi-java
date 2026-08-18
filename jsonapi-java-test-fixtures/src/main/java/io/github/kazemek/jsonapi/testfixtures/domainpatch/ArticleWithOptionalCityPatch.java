package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Shared direct typed PATCH DTO with a nested {@code PatchPresence<Optional<String>>} member. */
@JsonApiResource(type = "articles")
public record ArticleWithOptionalCityPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<AddressWithOptionalCityPatch> address) {}
