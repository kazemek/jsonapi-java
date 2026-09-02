package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Shared direct typed PATCH DTO with an invalid direct-{@code Present} nested shape (ADR-014). */
@JsonApiResource(type = "articles")
public record ArticleWithDirectPresentAddressPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<DirectPresentAddressPatch> address) {}
