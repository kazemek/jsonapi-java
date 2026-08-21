package io.github.kazemek.jsonapi.testfixtures.enveloperead;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Flat read-side DTO whose attribute conversion can fail for non-numeric wire values. */
@JsonApiResource(type = "strict-articles")
public record FlatStrictArticle(@JsonApiId String id, @JsonApiAttribute int title) {}
