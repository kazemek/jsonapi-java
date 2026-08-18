package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Low-level PATCH DTO wrapping {@link AddressWithLoudNote} (ADR-014 atomic-boundary coverage). */
@JsonApiResource(type = "articles")
public record AddressWithLoudNoteArticle(
    @JsonApiId String id, @JsonApiAttribute AddressWithLoudNote address) {}
