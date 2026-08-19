package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Invalid declaration: two resource meta properties on one mapping. */
@JsonApiResource(type = "articles")
public record DuplicateMetaArticle(
    @JsonApiId String id, @JsonApiMeta String meta, @JsonApiMeta String otherMeta) {}
