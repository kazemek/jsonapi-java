/**
 * Jackson 3 codecs for validating, writing, and reading JSON:API document envelopes, and for
 * mapping annotated domain types to resource objects.
 *
 * <p>Java {@code null} on model components means member absence. Explicit JSON {@code null} uses
 * sealed variants such as {@link io.github.kazemek.jsonapi.core.model.DocumentData.NullData}. Use
 * {@link JsonApiJackson3#writer} and {@link JsonApiJackson3#reader} as the sole public codec paths;
 * writers validate before emission, and readers validate before returning a document.
 *
 * <p>Domain-to-resource mapping uses {@link JsonApiJackson3#resourceMapper} to derive a {@link
 * JsonApiResourceMapper} from a caller's {@link tools.jackson.databind.json.JsonMapper} or builder.
 * Mapping is explicit, respects Jackson's logical property model, and never mutates the caller's
 * mapper. Mapping diagnostics use {@link MappingDiagnostic} stable codes; identifier conversion is
 * pluggable through {@link IdentifierConverter}.
 *
 * <p>Compound inclusion is opt-in via {@link CompoundSerializationContext} on the mapper's
 * three-argument overloads. Relationship mapping produces linkage only; included resources require
 * an explicit include request and {@link IncludePolicy}.
 */
@NullMarked
package io.github.kazemek.jsonapi.jackson3;

import org.jspecify.annotations.NullMarked;
