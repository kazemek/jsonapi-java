package io.github.kazemek.jsonapi.testfixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "comments")
public record Comment(
    @JsonApiId String id, @Nullable String body, @JsonApiRelationship @Nullable Person author) {}
