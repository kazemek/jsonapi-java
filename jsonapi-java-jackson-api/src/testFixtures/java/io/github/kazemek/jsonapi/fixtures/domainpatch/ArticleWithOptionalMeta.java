package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import java.util.Optional;
import org.jspecify.annotations.Nullable;

/** Ordinary domain model with an {@link Optional}-wrapped resource meta target (ADR-015). */
@JsonApiResource(type = "articles")
public record ArticleWithOptionalMeta(
    @JsonApiId String id,
    @JsonApiAttribute @Nullable String title,
    @JsonApiRelationship @Nullable ResourceIdentifier author,
    @JsonApiMeta Optional<ArticleMeta> meta,
    @JsonApiRelationshipMeta(relationship = "author") Optional<AuthorMeta> authorMeta) {}
