package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Shared low-level PATCH DTO with a {@code Set}/{@code array}/{@code Map} structured attribute. */
@JsonApiResource(type = "articles")
public record ArticleWithContainerAddress(
    @JsonApiId String id, @JsonApiAttribute AddressWithContainers address) {}
