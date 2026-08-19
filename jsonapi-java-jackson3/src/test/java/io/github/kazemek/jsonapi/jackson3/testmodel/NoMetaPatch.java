package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Typed PATCH DTO without any meta member: supplied meta must be rejected (ADR-015). */
@JsonApiResource(type = "articles")
public record NoMetaPatch(
    @JsonApiId String id,
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiRelationship PatchPresence<ResourceIdentifier> author) {}
