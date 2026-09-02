package io.github.kazemek.jsonapi.fixtures.compoundwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;

/** Article whose comments relationship is declared as {@link BaseComment}. */
@JsonApiResource(type = "articles")
public record PolymorphicArticle(
    @JsonApiId String id,
    @JsonApiAttribute String title,
    @JsonApiRelationship List<BaseComment> comments) {}
