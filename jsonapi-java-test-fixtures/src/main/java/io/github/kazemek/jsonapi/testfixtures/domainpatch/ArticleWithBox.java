package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/**
 * Shared low-level PATCH DTO with a generically-typed ordinary structured {@link Box} attribute.
 */
@JsonApiResource(type = "articles")
public record ArticleWithBox(@JsonApiId String id, @JsonApiAttribute Box box) {}
