package io.github.kazemek.jsonapi.fixtures.enveloperead;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Registry-rejection fixture: {@code @JsonApiResource} with an empty type name. */
@JsonApiResource(type = "")
public record EmptyResourceType() {}
