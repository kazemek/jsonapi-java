package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "people")
public record Person(@JsonApiId String id, @Nullable String name) {}
