/**
 * Immutable JSON:API v1.1 document model that preserves wire-visible states.
 *
 * <p>Java {@code null} on a containing component means a member is absent. Sealed value types such
 * as {@link io.github.kazemek.jsonapi.core.model.DocumentData} and {@link
 * io.github.kazemek.jsonapi.core.model.RelationshipData} represent explicit JSON {@code null},
 * single, and collection forms. Additional members hold pass-through extension and {@code @} names
 * without treating them as attributes, relationships, or links.
 *
 * <p>See ADR-002 and {@code docs/conformance.md} for the representation contract. Local
 * construction invariants are enforced here; aggregate rules require {@link
 * io.github.kazemek.jsonapi.core.validation.JsonApiDocumentValidator}.
 */
package io.github.kazemek.jsonapi.core.model;
