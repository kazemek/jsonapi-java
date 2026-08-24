package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Shared low-level PATCH DTO with a generic ordinary structured {@link Box}<Integer> attribute. */
@JsonApiResource(type = "articles")
public record ArticleWithBox(@JsonApiId String id, @JsonApiAttribute Box<Integer> box) {}
