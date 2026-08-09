package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Registry-rejection fixture: {@code @JsonApiResource} with a member-name-invalid type name. */
@JsonApiResource(type = "no:good:type")
public record InvalidResourceType() {}
