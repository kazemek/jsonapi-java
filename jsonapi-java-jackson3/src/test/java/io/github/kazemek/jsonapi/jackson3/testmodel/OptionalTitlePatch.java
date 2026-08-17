package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Optional;

/** Compatible Optional value-type patch DTO. */
@JsonApiResource(type = "articles")
public record OptionalTitlePatch(@JsonApiAttribute PatchPresence<Optional<String>> title) {}
