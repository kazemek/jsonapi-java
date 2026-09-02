package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/**
 * Shared low-level PATCH DTO whose {@code PatchPresence<T>} member wraps a presence-aware PATCH
 * shape: presence-aware PATCH shapes are a typed-path concept, so this composition fails loudly
 * with {@link
 * io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic#INVALID_PATCH_PROPERTY_TYPE} at
 * the attribute pointer (ADR-014).
 */
@JsonApiResource(type = "articles")
public record PatchPresenceAddressPatchArticle(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<AddressPatch> address) {}
