package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Optional;

/** Command DTO with an Optional attribute for generic value-type projection tests. */
@JsonApiResource(type = "articles")
public record OptionalTitleArticle(
    @JsonApiId String id, @JsonApiAttribute Optional<String> title) {}
