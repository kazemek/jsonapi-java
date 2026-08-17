package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Generic flat DTO whose attribute type must resolve from the bound parameterization. */
@JsonApiResource(type = "things")
public record GenericValue<T>(@JsonApiId String id, @JsonApiAttribute T value) {}
