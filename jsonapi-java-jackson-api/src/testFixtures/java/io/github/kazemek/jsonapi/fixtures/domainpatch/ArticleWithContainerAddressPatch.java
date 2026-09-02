package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Shared direct typed PATCH DTO with {@code Set}/{@code array}/{@code Map} nested members. */
@JsonApiResource(type = "articles")
public record ArticleWithContainerAddressPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<AddressWithContainersPatch> address) {}
