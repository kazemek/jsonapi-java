package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Generic flat DTO whose relationship target must resolve from the bound parameterization. */
@JsonApiResource(type = "articles")
public record GenericArticle<T>(@JsonApiId String id, @JsonApiRelationship T author) {}
