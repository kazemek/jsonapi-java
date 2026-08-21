package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Direct typed PATCH DTO wrapping a JavaBean-style nested {@link MutableAddressPatch} shape,
 * proving typed-path recursion applies to ordinary Jackson-bean semantics, not records specifically
 * (ADR-014).
 */
@JsonApiResource(type = "articles")
public record MutableArticleWithAddressPatch(
    @JsonApiId String id, @JsonApiAttribute PatchPresence<MutableAddressPatch> address) {}
