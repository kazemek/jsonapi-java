package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.ResourceObject;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Included resources plus whether any relationship was omitted by an applied fieldset while
 * building those included {@link ResourceObject}s.
 */
public record IncludedResourcesResult(
    @Nullable List<ResourceObject> included, boolean relationshipOmittedByFieldset) {}
