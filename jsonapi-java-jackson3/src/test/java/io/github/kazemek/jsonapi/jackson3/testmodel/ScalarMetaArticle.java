package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Invalid declaration: scalar whole-meta target. */
@JsonApiResource(type = "articles")
public record ScalarMetaArticle(@JsonApiId String id, @JsonApiMeta String meta) {}
