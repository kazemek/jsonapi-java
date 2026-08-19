package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Typed PATCH model whose relationship-meta member fails during final DTO construction. */
@JsonApiResource(type = "articles")
public record ThrowingRelMetaPatchArticle(
    @JsonApiId String id,
    @JsonApiRelationship PatchPresence<ResourceIdentifier> author,
    @JsonApiRelationshipMeta("author") PatchPresence<ThrowingRelMetaPatch> authorMeta) {}
