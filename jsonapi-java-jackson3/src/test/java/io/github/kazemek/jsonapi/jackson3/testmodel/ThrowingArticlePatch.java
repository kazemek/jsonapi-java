package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Typed PATCH DTO whose canonical constructor always throws, forcing a Jackson construction failure
 * with no property path so the shape-translated pointer falls back to the root (ADR-014).
 */
@JsonApiResource(type = "articles")
public record ThrowingArticlePatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<String> title) {

  public ThrowingArticlePatch {
    throw new IllegalStateException("boom");
  }
}
