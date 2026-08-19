package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;

/** Invalid declaration: list whole-meta target. */
@JsonApiResource(type = "articles")
public record ListMetaArticle(@JsonApiId String id, @JsonApiMeta List<String> meta) {}
