package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Shared low-level PATCH DTO with an ordinary structured {@link Address} attribute. */
@JsonApiResource(type = "articles")
public record Article(@JsonApiId String id, @JsonApiAttribute Address address) {}
