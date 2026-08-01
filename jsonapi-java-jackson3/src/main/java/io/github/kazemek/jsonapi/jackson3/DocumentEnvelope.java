package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import org.jspecify.annotations.Nullable;

/**
 * Carries optional document-level members — links, meta, and JSON:API object — into {@link
 * JsonApiResourceMapper#toDocument(Object, DocumentEnvelope)}.
 *
 * <p>Each component may be {@code null}; absent members are omitted from the serialized document.
 */
public record DocumentEnvelope(
    @Nullable Links links, @Nullable Meta meta, @Nullable JsonApiObject jsonapi) {}
