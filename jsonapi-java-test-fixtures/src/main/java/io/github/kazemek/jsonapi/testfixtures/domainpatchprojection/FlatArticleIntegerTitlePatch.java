package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO whose {@code title} value type is incompatible with the command mapping. */
@JsonApiResource(type = "articles")
public record FlatArticleIntegerTitlePatch(@JsonApiAttribute PatchPresence<Integer> title) {}
