package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Invalid direct typed PATCH DTO: a member typed as the concrete {@code Present} variant. */
@JsonApiResource(type = "articles")
public record DirectPresentPatch(@JsonApiId String id, PatchPresence.Present<String> title) {}
