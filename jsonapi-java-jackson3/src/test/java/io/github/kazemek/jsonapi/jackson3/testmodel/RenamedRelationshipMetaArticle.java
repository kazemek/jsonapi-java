package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorMeta;

/** Write/low-level model with a renamed relationship whose meta references the wire name. */
@JsonApiResource(type = "articles")
public record RenamedRelationshipMetaArticle(
    @JsonApiId String id,
    @JsonApiAttribute String title,
    @JsonApiRelationship(name = "author") ResourceIdentifier writtenBy,
    @JsonApiMeta ArticleMeta meta,
    @JsonApiRelationshipMeta("author") AuthorMeta authorMeta) {}
