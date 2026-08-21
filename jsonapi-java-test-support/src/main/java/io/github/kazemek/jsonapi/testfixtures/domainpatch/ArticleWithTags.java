package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;

/** Shared low-level PATCH DTO with a container attribute, proving atomic container boundaries. */
@JsonApiResource(type = "articles")
public record ArticleWithTags(@JsonApiId String id, @JsonApiAttribute List<String> tags) {}
