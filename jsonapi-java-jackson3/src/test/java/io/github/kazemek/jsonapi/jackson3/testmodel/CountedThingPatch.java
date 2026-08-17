package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO for primitive {@code int} command attributes boxed as {@code Integer}. */
@JsonApiResource(type = "things")
public record CountedThingPatch(@JsonApiAttribute PatchPresence<Integer> count) {}
