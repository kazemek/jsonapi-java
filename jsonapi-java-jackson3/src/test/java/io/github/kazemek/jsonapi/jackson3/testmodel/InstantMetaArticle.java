package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.time.Instant;

/** Invalid declaration: java.time whole-meta target (scalar, not bean-shaped). */
@JsonApiResource(type = "articles")
public record InstantMetaArticle(@JsonApiId String id, @JsonApiMeta Instant meta) {}
