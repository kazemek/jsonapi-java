/**
 * Shared implementation helpers for the core document model and validation.
 *
 * <p>This package is not a public API. It hosts URI/media-type/link syntax validators, ordered
 * null-preserving collection copies, and additional-member copy helpers used by {@code core.model}
 * and {@code core.validation}. See ADR-009 for nullness policy.
 */
@NullMarked
package io.github.kazemek.jsonapi.core.internal;

import org.jspecify.annotations.NullMarked;
