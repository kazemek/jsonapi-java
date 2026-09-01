package io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.mapping.RelationshipLinkage;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Compound-inclusion graph whose relationship targets are wrapped in {@link RelationshipLinkage} so
 * include traversal walks the unwrapped {@link Person} and {@link Comment} resources (ADR-017).
 */
@JsonApiResource(type = "articles")
public record WrappedLinkageArticle(
    @JsonApiId String id,
    @JsonApiRelationship @Nullable RelationshipLinkage<Person, AuthorIdMeta> author,
    @JsonApiRelationship @Nullable List<RelationshipLinkage<Comment, CommentIdMeta>> comments) {}
