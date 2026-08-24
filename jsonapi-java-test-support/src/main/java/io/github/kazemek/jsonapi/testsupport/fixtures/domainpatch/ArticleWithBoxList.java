package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;

/**
 * Shared low-level PATCH DTO with a nested generic {@link Box}<List<Integer>> attribute, proving a
 * nested type-variable binding (Box's {@code T} = {@code List<Integer>}) survives recursive shape
 * resolution (ADR-014).
 */
@JsonApiResource(type = "articles")
public record ArticleWithBoxList(@JsonApiId String id, @JsonApiAttribute Box<List<Integer>> box) {}
