package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.List;

/**
 * Patch DTO whose {@code comments} collection value type is incompatible with the command mapping.
 */
@JsonApiResource(type = "articles")
public record FlatArticleStringCommentsPatch(
    @JsonApiRelationship PatchPresence<List<String>> comments) {}
