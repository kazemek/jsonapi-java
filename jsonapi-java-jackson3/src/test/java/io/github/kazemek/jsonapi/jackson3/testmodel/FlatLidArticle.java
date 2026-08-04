package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Flat read-side DTO bound from a lid-only resource object. */
@JsonApiResource(type = "articles")
public record FlatLidArticle(@JsonApiId String id, String title) {}
