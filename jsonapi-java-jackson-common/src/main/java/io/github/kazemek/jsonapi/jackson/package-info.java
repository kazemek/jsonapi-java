/**
 * Jackson-major-neutral public contracts shared by the Jackson 2 and Jackson 3 codec adapters.
 *
 * <p>This package carries codec and domain-mapping policy, diagnostics, contexts, domain envelope
 * values, and presence-aware update-command contracts: {@link CompoundSerializationContext}, {@link
 * DocumentReadContext}, {@link DocumentEnvelope}, {@link MappedDocument}, {@link IncludePath},
 * {@link IncludePolicy}, {@link FieldPolicy}, {@link DomainData}, {@link IncludedResources}, {@link
 * IdentifierConverter}, {@link PatchCommand}, {@link PatchChange}, {@link PatchPresence}, {@link
 * RelationshipLinkage}, {@link StructuredPatch}, {@link StructuredMember}, {@link
 * StructuredMemberState}, {@link JsonApiMappingException}, {@link JsonApiDocumentReadException},
 * {@link MappingDiagnostic}, {@link CodecFailureCategory}, {@link SourceLocation}, and the
 * supporting allowance and kind types.
 *
 * <p>Mapping-diagnostic locations are major-neutral: a mapping failure carries either an absent
 * location or a valid JSON Pointer built through {@link MappingLocation}. Producers address one
 * resource object with resource-relative pointers over JSON:API member names; absence is {@code
 * null}, never {@code ""} or {@code /}; segments are individually RFC 6901-escaped. See {@link
 * JsonApiMappingException} for the full coordinate contract.
 *
 * <p>The package is deliberately Jackson-import-free: no {@code tools.jackson.*} or {@code
 * com.fasterxml.jackson.*} type appears in any production signature. Jackson-bound readers,
 * writers, mapping introspection, serializers, binders, module registration, and mapper factories
 * stay in the major-specific adapter packages; only neutral values cross through this package.
 *
 * <p>Nullness: for document/envelope/codec contracts, Java {@code null} means member absence and
 * sealed variants represent explicit JSON {@code null}; for presence-aware PATCH, {@link
 * PatchChange} entries in {@code changes()} are present and explicit attribute JSON {@code null} /
 * relationship NullLinkage use {@code @Nullable value == null} (no sealed attribute-null variant).
 * Direct PATCH DTO members declare presence through {@link PatchPresence}, whose {@link
 * PatchPresence.Present} with a {@code null} value is explicit null, never omission. Recursive
 * structured attributes use {@link StructuredPatch} / {@link StructuredMember} (wire and logical
 * member names) / {@link StructuredMemberState} (Atomic / Structured) as the neutral
 * requested-change payload (ADR-014); an empty {@code StructuredPatch} is a supplied empty
 * structured object, never a clear-all. {@code @Nullable} marks intentionally null-bearing members
 * per ADR-009.
 */
@NullMarked
package io.github.kazemek.jsonapi.jackson;

import org.jspecify.annotations.NullMarked;
