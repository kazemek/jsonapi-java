package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Optional;

/**
 * Shared direct typed PATCH DTO proving presence is separate from inner {@link Optional} nullness.
 */
@JsonApiResource(type = "articles")
public record OptionalPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<Optional<String>> subtitle) {}
