package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiIdentifierMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Typed PATCH DTO that illegally declares independently patchable identifier meta. Shared catalogs
 * use this to prove the explicit unsupported contract (ADR-017).
 */
@JsonApiResource(type = "articles")
public record ArticleWithIdentifierMetaPatch(
    @JsonApiId String id,
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiRelationship PatchPresence<ResourceIdentifier> author,
    @JsonApiIdentifierMeta("author") PatchPresence<AuthorIdMeta> authorIdMeta) {}
