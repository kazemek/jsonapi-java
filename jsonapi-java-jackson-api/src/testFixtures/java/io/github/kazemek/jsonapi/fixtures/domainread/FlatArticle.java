package io.github.kazemek.jsonapi.fixtures.domainread;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** Flat read-side DTO with built-in ResourceIdentifier relationship shapes. */
@JsonApiResource(type = "articles")
public record FlatArticle(
    @JsonApiId String id,
    @JsonApiAttribute @Nullable String title,
    @JsonApiAttribute @JsonProperty("body-text") @Nullable String body,
    @JsonApiRelationship @Nullable ResourceIdentifier author,
    @JsonApiRelationship @Nullable List<ResourceIdentifier> comments) {}
