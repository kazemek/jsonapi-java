package io.github.kazemek.jsonapi.testfixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Uses conventional "id" property (no explicit @JsonApiId). */
@JsonApiResource(type = "conventionals")
public record ConventionalId(String id, String name) {}
