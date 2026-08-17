package io.github.kazemek.jsonapi.testfixtures.domainpatchprojection;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO with the wrong JSON:API resource type for negative projection scenarios. */
@JsonApiResource(type = "people")
public record FlatWrongTypeArticlePatch(@JsonApiAttribute PatchPresence<String> title) {}
