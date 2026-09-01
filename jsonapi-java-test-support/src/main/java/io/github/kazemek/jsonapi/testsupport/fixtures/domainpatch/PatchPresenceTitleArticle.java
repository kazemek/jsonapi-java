package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/**
 * Shared low-level PATCH DTO with a scalar {@code PatchPresence<T>}-declared member, proving the
 * single-wrapper unwrap on the low-level path (ADR-014).
 */
@JsonApiResource(type = "articles")
public record PatchPresenceTitleArticle(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<String> title) {}
