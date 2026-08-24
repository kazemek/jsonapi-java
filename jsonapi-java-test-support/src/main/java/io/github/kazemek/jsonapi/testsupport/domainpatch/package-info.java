/**
 * Shared presence-aware PATCH fixtures: {@link PatchScenario} and {@link PatchDtoScenario} catalog
 * entries and expectation types reused by Jackson-major contract tests.
 *
 * <p>Payloads are Jackson-major-neutral JSON documents plus expected changes or diagnostics. Shared
 * flat DTOs live in {@code domainread} / {@code domainwrite}; this package adds PATCH-specific
 * types only when those cannot express a catalog entry (the direct PATCH DTOs carry {@link
 * io.github.kazemek.jsonapi.jackson.PatchPresence} members).
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.domainpatch;

import org.jspecify.annotations.NullMarked;
