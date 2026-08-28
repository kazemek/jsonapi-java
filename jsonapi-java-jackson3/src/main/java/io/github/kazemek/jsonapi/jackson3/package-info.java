/**
 * Jackson 3 codecs for validating, writing, and reading JSON:API document envelopes, and for
 * mapping annotated domain types to resource objects.
 *
 * <p>Java {@code null} on model components means member absence. Explicit JSON {@code null} uses
 * sealed variants such as {@link io.github.kazemek.jsonapi.core.model.DocumentData.NullData}. Use
 * {@link JsonApiJackson3#writer} and {@link JsonApiJackson3#reader} as the sole public codec paths;
 * writers validate before emission, and readers validate before returning a document.
 *
 * <p>Jackson-major adapters use a configured {@link tools.jackson.databind.json.JsonMapper} as the
 * canonical construction input. Capability-specific contexts and policy objects remain explicit,
 * and convenience factories choose documented defaults on top of the mapper-instance seam; {@code
 * JsonMapper.Builder} overloads are intentionally not part of the public contract. Token-driven
 * document reading uses the supplied mapper directly; capabilities that need adapter modules or
 * isolated introspection state derive a mapper internally and never mutate the caller. This
 * mapper-instance construction rule is the semantic reference for the future Jackson 2 adapter and
 * for Spring integration; parity does not require duplicating this facade's convenience overloads.
 *
 * <p>Domain-to-resource mapping uses {@link JsonApiJackson3#resourceMapper} to derive a {@link
 * JsonApiResourceMapper} from the caller's configured mapper. Mapping is explicit, respects
 * Jackson's logical property model, and never mutates the caller's mapper. Configured Jackson is
 * authoritative for class-level resource metadata: {@code JsonApiResource} is resolved through
 * mapper introspection, so class-level mix-ins provide or override it across domain write, flat
 * binding, PATCH binding, registry key derivation, and declared to-many element types. Mapping
 * diagnostics use {@link MappingDiagnostic} stable codes; identifier conversion is pluggable
 * through {@link IdentifierConverter}. Read-side flat DTO binding uses {@link
 * JsonApiJackson3#resourceBinder} to derive a {@link JsonApiResourceBinder}; relationship linkage
 * conversion is pluggable through {@link RelationshipLinkageMapper}. Convenience domain writes
 * infer a root {@link tools.jackson.databind.JavaType} from a concrete runtime class. Directly
 * parameterized roots require the mapper overloads that accept a complete {@code JavaType}; that
 * type remains authoritative for generic attributes, relationship targets, and compound inclusion.
 * Concrete subclasses with resolvable generic superclass bindings remain eligible for the
 * convenience route, while unresolved mapped variables fail at their JSON:API member location
 * instead of being inferred from runtime contents.
 *
 * <p>Configured Jackson is also authoritative at the mapped-property boundary for ordinary values:
 * attributes and mapped resource/relationship meta use contextualized property serializers on
 * write, and {@code RelationshipLinkage} identifier meta converts through configured Jackson
 * against the wrapper's meta {@code JavaType}. Flat reads plus supplied PATCH values use
 * contextualized property deserializers after JSON:API-specific conversion. JSON:API remains
 * authoritative for the identifier wire string, relationship linkage, and {@link
 * io.github.kazemek.jsonapi.jackson.PatchPresence} state; those adapter-owned wire states are not
 * replaced by property customization. Type/module conversion is retained as the fallback when no
 * mapped property can be resolved.
 *
 * <p>Typed domain envelopes use {@link JsonApiJackson3#domainDocumentReader} with an explicit
 * {@link ResourceTypeRegistry}: a dispatch-only registry keyed by each registered target's
 * configured class-level resource metadata (build via {@link
 * ResourceTypeRegistry#builder(tools.jackson.databind.json.JsonMapper)}); a {@link
 * JsonApiDomainDocument} carries flat primary DTOs and independently bound {@code included} DTOs
 * (wire-ordered, dual id/lid identity lookup, never injected into relationships). Identifier
 * primary data stays as core {@link io.github.kazemek.jsonapi.core.model.ResourceIdentifier} values
 * and error documents never bind.
 *
 * <p>Presence-aware resource updates use {@link JsonApiJackson3#patchReader} to derive a {@link
 * JsonApiPatchReader} that applies validate-on-read with {@code DocumentUsage.UPDATE_REQUEST} and
 * binds only supplied mapped members into a common {@link
 * io.github.kazemek.jsonapi.jackson.PatchCommand} without constructing a DTO or reading {@code
 * included}. Direct typed PATCH DTO binding uses {@link JsonApiJackson3#patchDtoReader} to derive a
 * {@link JsonApiPatchDtoReader} that binds the validated update directly into an application-owned
 * annotated PATCH DTO whose patchable members are declared as {@link
 * io.github.kazemek.jsonapi.jackson.PatchPresence}.
 *
 * <p>Compound inclusion is opt-in via {@link CompoundSerializationContext} on the mapper's
 * three-argument overloads. Relationship mapping produces linkage only; included resources require
 * an explicit include request and {@link IncludePolicy}. Sparse fieldsets share that context
 * ({@code fieldsets} + {@link FieldPolicy}) and are applied only by the {@link MappedDocument}
 * overloads; write the returned {@link MappedDocument} through a {@link JsonApiDocumentWriter} and
 * the writer composes its bound validation policy with the mapped sparse-fieldset linkage
 * exemptions—callers never translate mapping provenance into a validation context themselves.
 * Exemptions name exactly the included resources whose linking relationship a fieldset removed, so
 * unrelated full-linkage defects still fail validation. Fieldsets select attributes and
 * relationships only; whole-object resource meta is emitted independently (ADR-015).
 *
 * <p>Whole-object resource-side meta mapping (ADR-015) maps the complete {@code meta} object of a
 * resource or of a specific mapped relationship to one application-owned property per location via
 * {@link io.github.kazemek.jsonapi.annotation.JsonApiMeta} and {@link
 * io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta}, across domain read, domain write,
 * the low-level {@link io.github.kazemek.jsonapi.jackson.PatchCommand} path (new resource-meta and
 * relationship-meta {@link io.github.kazemek.jsonapi.jackson.PatchChange} variants), and the typed
 * PATCH DTO path. Per-linkage identifier meta is an opt-in {@link
 * io.github.kazemek.jsonapi.jackson.RelationshipLinkage} (ADR-017): {@code target} maps as the
 * ordinary relationship target and {@code meta} maps to {@code ResourceIdentifier.meta}. PATCH
 * participates only through whole-linkage replacement — never as an independent {@code PatchChange}
 * or typed PATCH member. Document-level meta remains document-owned through the domain envelope; no
 * resource annotation means document meta.
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
