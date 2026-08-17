package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Subset patch DTO exposing only {@code title}. */
@JsonApiResource(type = "articles")
public record FlatArticleTitleOnlyPatch(@JsonApiAttribute PatchPresence<String> title) {}
