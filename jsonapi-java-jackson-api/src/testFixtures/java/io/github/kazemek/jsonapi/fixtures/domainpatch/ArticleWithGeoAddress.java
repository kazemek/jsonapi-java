package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/**
 * Shared low-level PATCH DTO with a multi-level ordinary structured {@link AddressWithGeo}
 * attribute.
 */
@JsonApiResource(type = "articles")
public record ArticleWithGeoAddress(
    @JsonApiId String id, @JsonApiAttribute AddressWithGeo address) {}
