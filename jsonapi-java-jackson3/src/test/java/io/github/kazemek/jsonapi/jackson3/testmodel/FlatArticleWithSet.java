package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import java.util.Set;

/** Flat read-side DTO with a Set-based to-many ResourceIdentifier relationship. */
@JsonApiResource(type = "articles")
public record FlatArticleWithSet(
    @JsonApiId String id, String title, @JsonApiRelationship Set<ResourceIdentifier> tags) {}
