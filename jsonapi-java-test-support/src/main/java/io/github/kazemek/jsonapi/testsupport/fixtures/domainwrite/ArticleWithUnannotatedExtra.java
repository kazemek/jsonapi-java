package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/**
 * Jackson-visible extra property with no JSON:API role. It must not participate as an attribute.
 */
@JsonApiResource(type = "articles")
public record ArticleWithUnannotatedExtra(
    @JsonApiId String id, @JsonApiAttribute String title, @Nullable String ignoredExtra) {}
