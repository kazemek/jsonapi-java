package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Invalid direct typed PATCH DTO: the identifier must never be a patchable {@code PatchPresence}.
 */
@JsonApiResource(type = "articles")
public record PresenceIdPatch(@JsonApiId PatchPresence<String> id) {}
