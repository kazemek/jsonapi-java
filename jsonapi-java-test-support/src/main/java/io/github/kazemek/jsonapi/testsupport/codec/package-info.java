/**
 * Shared codec scenario catalog: capability-tagged documents, negative scenarios, and dual-success
 * ambiguous primary-data scenarios consumed by Jackson-major contract tests.
 *
 * <p>Types in this package are major-neutral (see ADR-004 and ADR-010): they import only core,
 * Jackson common, JSpecify, JDK, and JSON-P types — never {@code tools.jackson..} or {@code
 * com.fasterxml.jackson.databind..}. Fixture ids and expected JSON paths are stable across majors.
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}; nullable catalog
 * members such as {@code CodecScenario.primaryDataKind} and {@code NegativeCodecScenario.pointer}
 * carry {@link org.jspecify.annotations.Nullable}.
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.codec;

import org.jspecify.annotations.NullMarked;
