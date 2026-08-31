package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/**
 * Unannotated ordinary property: it does not participate in JSON:API mapping. A supplied {@code
 * note} attribute is therefore an unknown typed PATCH member.
 */
@JsonApiResource(type = "articles")
public record UnannotatedPatch(@JsonApiId String id, String note) {}
