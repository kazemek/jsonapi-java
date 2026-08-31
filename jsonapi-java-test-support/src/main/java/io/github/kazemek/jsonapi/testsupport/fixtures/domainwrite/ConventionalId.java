package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Uses conventional "id" property (no explicit @JsonApiId). */
@JsonApiResource(type = "conventionals")
public record ConventionalId(String id, @JsonApiAttribute String name) {}
