package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;

/** Flat read-side DTO whose relationships target a custom mapper type. */
@JsonApiResource(type = "articles")
public record FlatMappedArticle(
    @JsonApiId String id,
    String title,
    @JsonApiRelationship FlatAuthor author,
    @JsonApiRelationship List<FlatAuthor> contributors) {}
