package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Optional;

/**
 * Shared direct typed PATCH DTO whose structured attribute is an {@code Optional}-wrapped
 * presence-aware shape, proving typed {@code Optional} unwrap/rewrap semantics (ADR-014).
 */
@JsonApiResource(type = "articles")
public record ArticleWithOptionalAddressPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<Optional<AddressPatch>> address) {}
