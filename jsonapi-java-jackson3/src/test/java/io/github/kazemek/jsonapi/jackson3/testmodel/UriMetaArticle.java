package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.net.URI;

/** Invalid declaration: URI whole-meta target (scalar, not bean-shaped). */
@JsonApiResource(type = "articles")
public record UriMetaArticle(@JsonApiId String id, @JsonApiMeta URI meta) {}
