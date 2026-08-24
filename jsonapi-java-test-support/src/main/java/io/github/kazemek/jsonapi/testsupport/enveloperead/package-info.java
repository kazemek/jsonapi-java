/**
 * Version-neutral typed-envelope read fixture catalog: shared envelope-only binding targets plus
 * {@link EnvelopeReadScenario} / {@link EnvelopeReadScenarios} consumed by Jackson-major domain
 * document reader contract tests.
 *
 * <p>Types in this package are major-neutral (see ADR-004 and ADR-010): they import only core,
 * annotations, Jackson common, JSpecify, and JDK types — never {@code tools.jackson..} or {@code
 * com.fasterxml.jackson.databind..}. Included resources bind independently and are never injected
 * into relationships (ADR-011). Flat binder DTOs such as {@code FlatArticle} remain in {@code
 * domainread}; write-side models such as {@code Person} remain in {@code domainwrite}.
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}; null-bearing DTO
 * members carry {@link org.jspecify.annotations.Nullable} to preserve omitted relationship and
 * attribute states the scenarios exercise.
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.enveloperead;

import org.jspecify.annotations.NullMarked;
