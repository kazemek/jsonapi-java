/**
 * Jackson-major-neutral public contracts shared by the Jackson 2 and Jackson 3 codec adapters.
 *
 * <p>This package carries codec and domain-mapping policy, diagnostics, contexts, and domain
 * envelope values: {@link CompoundSerializationContext}, {@link DocumentReadContext}, {@link
 * DocumentEnvelope}, {@link MappedDocument}, {@link IncludePath}, {@link IncludePolicy}, {@link
 * FieldPolicy}, {@link DomainData}, {@link IncludedResources}, {@link IdentifierConverter}, {@link
 * JsonApiMappingException}, {@link JsonApiDocumentReadException}, {@link MappingDiagnostic}, {@link
 * CodecFailureCategory}, {@link SourceLocation}, and the supporting allowance and kind types.
 *
 * <p>The package is deliberately Jackson-import-free: no {@code tools.jackson.*} or {@code
 * com.fasterxml.jackson.*} type appears in any production signature. Jackson-bound readers,
 * writers, mapping introspection, serializers, binders, module registration, and mapper factories
 * stay in the major-specific adapter packages; only neutral values cross through this package.
 *
 * <p>Nullness mirrors the core model: Java {@code null} means member absence, sealed variants
 * represent explicit JSON {@code null}, and {@code @Nullable} marks intentionally null-bearing
 * members per ADR-009.
 */
@NullMarked
package io.github.kazemek.jsonapi.jackson;

import org.jspecify.annotations.NullMarked;
