package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Shared low-level PATCH DTO whose {@code PatchPresence<T>}-declared member wraps an ordinary
 * structured bean, proving the single-wrapper unwrap recurses on the low-level path (ADR-014).
 */
@JsonApiResource(type = "articles")
public record PatchPresenceAddressArticle(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<Address> address) {}
