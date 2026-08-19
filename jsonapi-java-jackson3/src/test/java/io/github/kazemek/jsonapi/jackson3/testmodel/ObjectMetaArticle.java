package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Object-typed resource meta target: declared-valid, but the runtime value must be map-shaped. */
@JsonApiResource(type = "articles")
public record ObjectMetaArticle(@JsonApiId String id, @JsonApiMeta Object meta) {}
