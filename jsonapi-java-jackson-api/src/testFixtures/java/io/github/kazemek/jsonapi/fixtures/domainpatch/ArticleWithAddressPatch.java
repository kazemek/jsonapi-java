package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Shared direct typed PATCH DTO with a structured {@link AddressPatch} attribute. */
@JsonApiResource(type = "articles")
public record ArticleWithAddressPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<AddressPatch> address) {}
