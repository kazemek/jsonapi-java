package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.List;

/** Full mutable-surface patch DTO for {@code articles} projection scenarios. */
@JsonApiResource(type = "articles")
public record FlatArticlePatch(
    @JsonApiAttribute PatchPresence<String> title,
    @JsonApiAttribute(name = "body-text") PatchPresence<String> body,
    @JsonApiRelationship PatchPresence<ResourceIdentifier> author,
    @JsonApiRelationship PatchPresence<List<ResourceIdentifier>> comments) {}
