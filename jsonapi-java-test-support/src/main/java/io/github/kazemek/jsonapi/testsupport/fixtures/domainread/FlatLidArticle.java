package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/** Flat read-side DTO bound from a lid-only resource object. */
@JsonApiResource(type = "articles")
public record FlatLidArticle(
    @JsonApiId @Nullable String id, @JsonApiAttribute @Nullable String title) {}
