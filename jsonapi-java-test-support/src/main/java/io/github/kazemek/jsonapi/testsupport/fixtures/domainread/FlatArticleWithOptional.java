package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** Flat read-side DTO with an Optional to-one ResourceIdentifier relationship. */
@JsonApiResource(type = "articles")
public record FlatArticleWithOptional(
    @JsonApiId String id,
    @Nullable String title,
    @JsonApiRelationship Optional<ResourceIdentifier> author) {}
