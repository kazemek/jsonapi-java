package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/**
 * Shared low-level PATCH DTO with an ordinary structured {@link AddressWithOptionalCity} attribute.
 */
@JsonApiResource(type = "articles")
public record ArticleWithOptionalCity(
    @JsonApiId String id, @JsonApiAttribute AddressWithOptionalCity address) {}
