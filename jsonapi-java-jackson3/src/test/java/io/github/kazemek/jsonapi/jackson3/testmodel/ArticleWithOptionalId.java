package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Optional;

@JsonApiResource(type = "articles")
public record ArticleWithOptionalId(@JsonApiId Optional<String> id, String title) {}
