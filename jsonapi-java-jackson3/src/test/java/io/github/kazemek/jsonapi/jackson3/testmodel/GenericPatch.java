package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Generic direct typed PATCH DTO; parameterization must survive introspection and binding. */
@JsonApiResource(type = "articles")
public record GenericPatch<T>(@JsonApiId T id, @JsonApiAttribute PatchPresence<T> title) {}
