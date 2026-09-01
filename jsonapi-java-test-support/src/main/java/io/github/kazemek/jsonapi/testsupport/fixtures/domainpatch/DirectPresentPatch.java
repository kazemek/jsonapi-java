package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Invalid direct typed PATCH DTO: a member typed as the concrete {@code Present} variant. */
@JsonApiResource(type = "articles")
public record DirectPresentPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence.Present<String> title) {}
