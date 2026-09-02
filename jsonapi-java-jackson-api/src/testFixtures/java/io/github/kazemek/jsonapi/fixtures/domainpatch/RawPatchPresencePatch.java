package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Invalid direct typed PATCH DTO: a raw (unparameterized) {@code PatchPresence} member. */
@JsonApiResource(type = "articles")
@SuppressWarnings("rawtypes")
public record RawPatchPresencePatch(@JsonApiId String id, @JsonApiAttribute PatchPresence title) {}
