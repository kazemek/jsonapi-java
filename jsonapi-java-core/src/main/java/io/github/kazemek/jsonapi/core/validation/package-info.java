/**
 * Aggregate JSON:API document validation, stable diagnostics, and member-name grammar checks.
 *
 * <p>Local invariants are enforced when model values are constructed. {@link
 * io.github.kazemek.jsonapi.core.validation.MemberNames} validates JSON:API v1.1 member-name
 * grammar. {@link io.github.kazemek.jsonapi.core.validation.JsonApiDocumentValidator} and {@link
 * io.github.kazemek.jsonapi.core.validation.ValidationContext} validate rules that need full
 * document context (resource identity uniqueness, full linkage, local-identifier consistency,
 * link-member context, and extension/profile policy).
 *
 * <p>Failures carry a stable {@link io.github.kazemek.jsonapi.core.validation.ValidationRuleCode}
 * and a JSON Pointer-like path. See ADR-003, ADR-009, and {@code docs/conformance.md}.
 */
@NullMarked
package io.github.kazemek.jsonapi.core.validation;

import org.jspecify.annotations.NullMarked;
