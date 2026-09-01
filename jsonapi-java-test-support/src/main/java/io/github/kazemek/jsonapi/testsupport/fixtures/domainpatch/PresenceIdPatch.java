package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/**
 * Invalid direct typed PATCH DTO: the identifier must never be a patchable {@code PatchPresence}.
 */
@JsonApiResource(type = "articles")
public record PresenceIdPatch(@JsonApiId PatchPresence<String> id) {}
