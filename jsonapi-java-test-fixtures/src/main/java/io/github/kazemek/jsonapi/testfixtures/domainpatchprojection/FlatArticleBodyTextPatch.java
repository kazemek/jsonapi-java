package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Patch DTO whose Java component name differs from the command DTO while sharing JSON:API name
 * {@code body-text}.
 */
@JsonApiResource(type = "articles")
public record FlatArticleBodyTextPatch(
    @JsonApiAttribute(name = "body-text") PatchPresence<String> bodyText) {}
