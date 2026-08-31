package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Conventional {@code id} property with no {@code @JsonApiId}: the sole implicit JSON:API
 * property-role convention.
 */
@JsonApiResource(type = "articles")
public record ConventionalIdPatch(String id, @JsonApiAttribute PatchPresence<String> title) {}
