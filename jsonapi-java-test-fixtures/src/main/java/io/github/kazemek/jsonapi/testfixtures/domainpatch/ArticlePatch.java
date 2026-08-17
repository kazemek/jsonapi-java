package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.List;

/** Shared direct typed PATCH DTO with built-in {@link ResourceIdentifier} relationship shapes. */
@JsonApiResource(type = "articles")
public record ArticlePatch(
    @JsonApiId String id,
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiAttribute(name = "body-text") PatchPresence<String> body,
    @JsonApiRelationship PatchPresence<ResourceIdentifier> author,
    @JsonApiRelationship PatchPresence<List<ResourceIdentifier>> comments) {}
