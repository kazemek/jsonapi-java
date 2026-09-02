package io.github.kazemek.jsonapi.fixtures.sparsefieldset;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.fixtures.domainwrite.Person;

/** Article whose author relationship uses a renamed JSON:API member. */
@JsonApiResource(type = "articles")
public record ArticleWithRenamedAuthor(
    @JsonApiId String id,
    @JsonApiAttribute String title,
    @JsonApiRelationship @JsonProperty("written-by") Person author) {}
