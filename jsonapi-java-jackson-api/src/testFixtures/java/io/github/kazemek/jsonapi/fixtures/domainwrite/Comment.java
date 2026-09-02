package io.github.kazemek.jsonapi.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "comments")
public record Comment(
    @JsonApiId String id,
    @JsonApiAttribute @Nullable String body,
    @JsonApiRelationship @Nullable Person author) {}
