package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO that declares {@code Omitted<T>} instead of {@code PatchPresence<T>}. */
@JsonApiResource(type = "articles")
public record OmittedTitlePatch(@JsonApiAttribute PatchPresence.Omitted<String> title) {}
