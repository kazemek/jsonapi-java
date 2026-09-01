package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Shared direct typed PATCH DTO with an invalid mixed nested shape (ADR-014). */
@JsonApiResource(type = "articles")
public record ArticleWithMixedAddressPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<MixedAddressPatch> address) {}
