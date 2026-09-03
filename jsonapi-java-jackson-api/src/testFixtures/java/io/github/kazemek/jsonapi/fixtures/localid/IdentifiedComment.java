package io.github.kazemek.jsonapi.fixtures.localid;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiLocalId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/**
 * Passive, application-shaped related-resource carrier carrying both identity members, so linkage
 * extraction must preserve {@code id} and {@code lid} together.
 */
@JsonApiResource(type = "comments")
public record IdentifiedComment(
    @JsonApiId String id,
    @JsonApiLocalId String localId,
    @JsonApiAttribute @Nullable String body) {}
