package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Optional;

/**
 * Shared low-level PATCH DTO whose ordinary structured attribute is {@code Optional}-wrapped,
 * proving the transparent {@code Optional} qualification wrapper (ADR-014).
 */
@JsonApiResource(type = "articles")
public record ArticleWithOptionalAddress(
    @JsonApiId String id, @JsonApiAttribute Optional<Address> address) {}
