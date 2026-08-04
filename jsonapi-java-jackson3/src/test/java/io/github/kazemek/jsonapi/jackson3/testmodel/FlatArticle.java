package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import java.util.List;

/** Flat read-side DTO with built-in ResourceIdentifier relationship shapes. */
@JsonApiResource(type = "articles")
public record FlatArticle(
    @JsonApiId String id,
    @JsonApiAttribute String title,
    @JsonApiAttribute(name = "body-text") String body,
    @JsonApiRelationship ResourceIdentifier author,
    @JsonApiRelationship List<ResourceIdentifier> comments) {}
