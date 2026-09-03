package io.github.kazemek.jsonapi.fixtures.localid;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiLocalId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/**
 * Passive, application-shaped related-resource carrier identified only by a local identifier. Used
 * as a relationship target so linkage extraction must preserve {@code lid} without promoting it to
 * {@code id}.
 */
@JsonApiResource(type = "comments")
public record LocalIdOnlyComment(
    @JsonApiLocalId String localId, @JsonApiAttribute @Nullable String body) {}
