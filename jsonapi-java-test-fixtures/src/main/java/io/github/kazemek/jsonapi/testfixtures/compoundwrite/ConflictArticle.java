package io.github.kazemek.jsonapi.testfixtures.compoundwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person;

/** Article with two to-one people relationships for conflicting-representation tests. */
@JsonApiResource(type = "articles")
public record ConflictArticle(
    @JsonApiId String id,
    @JsonApiRelationship Person author,
    @JsonApiRelationship Person reviewer) {}
