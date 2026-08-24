package io.github.kazemek.jsonapi.testsupport.fixtures.enveloperead;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Registry-rejection fixture: {@code @JsonApiResource} with a member-name-invalid type name. */
@JsonApiResource(type = "no:good:type")
public record InvalidResourceType() {}
