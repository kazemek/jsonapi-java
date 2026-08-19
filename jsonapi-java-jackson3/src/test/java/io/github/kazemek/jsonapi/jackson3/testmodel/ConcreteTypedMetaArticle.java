package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Low-level domain model with a concrete root-polymorphic whole-meta target. */
@JsonApiResource(type = "articles")
public record ConcreteTypedMetaArticle(@JsonApiId String id, @JsonApiMeta ConcreteTypedMeta meta) {}
