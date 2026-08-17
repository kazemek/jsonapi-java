package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Optional;

/** Incompatible Optional value-type patch DTO. */
@JsonApiResource(type = "articles")
public record OptionalIntegerTitlePatch(@JsonApiAttribute PatchPresence<Optional<Integer>> title) {}
