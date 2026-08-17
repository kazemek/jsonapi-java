package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Parameterized patch DTO used to prove {@code JavaType} generic bindings are preserved. */
@JsonApiResource(type = "articles")
public record GenericTitlePatch<T>(@JsonApiAttribute PatchPresence<T> title) {}
