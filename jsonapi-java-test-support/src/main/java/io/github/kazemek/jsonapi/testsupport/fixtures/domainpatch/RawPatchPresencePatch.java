package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Invalid direct typed PATCH DTO: a raw (unparameterized) {@code PatchPresence} member. */
@JsonApiResource(type = "articles")
@SuppressWarnings("rawtypes")
public record RawPatchPresencePatch(@JsonApiId String id, PatchPresence title) {}
