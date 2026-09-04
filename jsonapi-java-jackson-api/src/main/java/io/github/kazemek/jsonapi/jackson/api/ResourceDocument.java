package io.github.kazemek.jsonapi.jackson.api;

import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Links;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.jackson.mapping.IncludedResources;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Optional Level-1 typed read result for one resource plus document-level state.
 *
 * <p>Carries the bound primary DTO together with top-level {@code meta}, {@code links}, {@code
 * jsonapi}, and independently bound compound {@code included} state. Java {@code null} components
 * mean the member was absent. Included resources are never hydrated into relationship properties.
 * The result is returned only after complete document validation. Error documents, identifier
 * primary data, and additional document members are not carried; reads requiring those states use
 * the documents facet or an advanced envelope instead.
 *
 * @param <T> the bound primary DTO type
 */
public record ResourceDocument<T>(
    T resource,
    @Nullable Meta meta,
    @Nullable Links links,
    @Nullable JsonApiObject jsonapi,
    @Nullable IncludedResources included) {

  public ResourceDocument {
    Objects.requireNonNull(resource, "resource");
  }
}
