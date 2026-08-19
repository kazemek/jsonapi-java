package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.UUID;

/** Invalid declaration: UUID whole-meta target (scalar, not bean-shaped). */
@JsonApiResource(type = "articles")
public record UuidMetaArticle(@JsonApiId String id, @JsonApiMeta UUID meta) {}
