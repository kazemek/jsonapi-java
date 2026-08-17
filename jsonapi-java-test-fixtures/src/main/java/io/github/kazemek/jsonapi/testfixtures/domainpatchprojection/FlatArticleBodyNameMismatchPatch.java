package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Patch DTO whose Java logical name matches the command DTO {@code body} property but whose
 * JSON:API name does not.
 */
@JsonApiResource(type = "articles")
public record FlatArticleBodyNameMismatchPatch(@JsonApiAttribute PatchPresence<String> body) {}
