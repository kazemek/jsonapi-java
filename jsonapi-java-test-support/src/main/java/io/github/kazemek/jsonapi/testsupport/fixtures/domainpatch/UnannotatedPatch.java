package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/**
 * Invalid direct typed PATCH DTO: an unannotated member defaults to the attribute role and must
 * therefore also be exactly {@code PatchPresence}.
 */
@JsonApiResource(type = "articles")
public record UnannotatedPatch(@JsonApiId String id, String note) {}
