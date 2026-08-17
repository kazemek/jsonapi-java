package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Patch DTO with a plain attribute type for negative projection scenarios. */
@JsonApiResource(type = "articles")
public record FlatArticlePlainTitlePatch(@JsonApiAttribute String title) {}
