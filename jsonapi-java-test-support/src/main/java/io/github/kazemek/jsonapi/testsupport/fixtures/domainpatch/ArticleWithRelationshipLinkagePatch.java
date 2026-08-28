package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import io.github.kazemek.jsonapi.jackson.RelationshipLinkage;
import java.util.List;

/**
 * Typed PATCH DTO that carries identifier meta only as part of whole-linkage {@link
 * RelationshipLinkage} replacement (ADR-017).
 */
@JsonApiResource(type = "articles")
public record ArticleWithRelationshipLinkagePatch(
    @JsonApiId String id,
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiRelationship
        PatchPresence<RelationshipLinkage<ResourceIdentifier, AuthorIdMeta>> author,
    @JsonApiRelationship
        PatchPresence<List<RelationshipLinkage<ResourceIdentifier, CommentIdMeta>>> comments) {}
