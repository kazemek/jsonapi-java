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
 * pluggable through {@link IdentifierConverter}. Read-side flat DTO binding uses {@link
 * JsonApiJackson3#resourceBinder} to derive a {@link JsonApiResourceBinder}; relationship linkage
 * conversion is pluggable through {@link RelationshipLinkageMapper}.
 *
 * <p>Typed domain envelopes use {@link JsonApiJackson3#domainDocumentReader} with an explicit
 * {@link ResourceTypeRegistry}: a {@link JsonApiDomainDocument} carries flat primary DTOs and
 * independently bound {@code included} DTOs (wire-ordered, dual id/lid identity lookup, never
 * injected into relationships). Identifier primary data stays as core {@link
 * io.github.kazemek.jsonapi.core.model.ResourceIdentifier} values and error documents never bind.
 *
 * <p>Presence-aware resource updates use {@link JsonApiJackson3#patchReader} to derive a {@link
 * JsonApiPatchReader} that applies validate-on-read with {@code DocumentUsage.UPDATE_REQUEST} and
 * binds only supplied mapped members into a common {@link
 * io.github.kazemek.jsonapi.jackson.PatchCommand} without constructing a DTO or reading {@code
 * included}.
 *
 * <p>Compound inclusion is opt-in via {@link CompoundSerializationContext} on the mapper's
 * three-argument overloads. Relationship mapping produces linkage only; included resources require
 * an explicit include request and {@link IncludePolicy}. Sparse fieldsets share that context
 * ({@code fieldsets} + {@link FieldPolicy}) and are applied only by the {@link MappedDocument}
 * overloads; pass {@link MappedDocument#applyTo} into the writer factory when relationships were
 * omitted by fieldset while inclusion still traversed them.
 *
 * <p>Codec and mapping policy, diagnostics, contexts, domain envelope values, and presence-aware
 * update commands are Jackson-major-neutral contracts in {@link io.github.kazemek.jsonapi.jackson};
 * this package holds only Jackson 3-bound factories, readers, writers, binders, and their
 * implementation.
 */
@NullMarked
package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext;
import io.github.kazemek.jsonapi.jackson.FieldPolicy;
import io.github.kazemek.jsonapi.jackson.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson.IncludePolicy;
import io.github.kazemek.jsonapi.jackson.MappedDocument;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import org.jspecify.annotations.NullMarked;
