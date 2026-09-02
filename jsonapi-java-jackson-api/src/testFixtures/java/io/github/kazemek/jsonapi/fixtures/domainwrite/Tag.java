package io.github.kazemek.jsonapi.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "tags")
public record Tag(@JsonApiId String name) {}
