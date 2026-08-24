package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Invalid direct typed PATCH DTO: a patchable member that is not {@code PatchPresence}. */
@JsonApiResource(type = "articles")
public record NonPatchPresencePatch(@JsonApiId String id, @JsonApiAttribute String title) {}
