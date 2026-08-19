package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/**
 * Low-level domain model whose meta bean carries a polymorphic property-level TypeDeserializer
 * member.
 */
@JsonApiResource(type = "articles")
public record ArticleWithTypedContactMeta(
    @JsonApiId String id, @JsonApiMeta OuterWithTypedContact meta) {}
