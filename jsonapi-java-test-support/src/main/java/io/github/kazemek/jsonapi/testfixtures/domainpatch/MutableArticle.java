package io.github.kazemek.jsonapi.testfixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/**
 * Low-level PATCH DTO wrapping a non-record JavaBean-style structured {@link MutableAddress}
 * attribute, proving the low-level traversable-bean boundary applies to ordinary Jackson-bean
 * semantics (ADR-014).
 */
@JsonApiResource(type = "articles")
public record MutableArticle(@JsonApiId String id, @JsonApiAttribute MutableAddress address) {}
