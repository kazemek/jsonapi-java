/**
 * Aggregate JSON:API document validation and stable diagnostics.
 *
 * <p>Local invariants are enforced when model values are constructed. This package validates rules
 * that need full document context (resource identity uniqueness, full linkage, local-identifier
 * consistency, link-member context, and extension/profile policy) via {@link
 * io.github.kazemek.jsonapi.core.validation.JsonApiDocumentValidator} and {@link
 * io.github.kazemek.jsonapi.core.validation.ValidationContext}.
 *
 * <p>Failures carry a stable {@link io.github.kazemek.jsonapi.core.validation.ValidationRuleCode}
 * and a JSON Pointer-like path. See ADR-003, ADR-009, and {@code docs/conformance.md}.
 */
@NullMarked
package io.github.kazemek.jsonapi.core.validation;

import org.jspecify.annotations.NullMarked;
