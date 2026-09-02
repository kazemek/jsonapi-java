package io.github.kazemek.jsonapi.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.patch.PatchPresence;

/** Shared direct typed PATCH DTO with an integer identifier (identifier conversion failures). */
@JsonApiResource(type = "things")
public record IntIdPatch(@JsonApiId Integer id, @JsonApiAttribute PatchPresence<String> name) {}
