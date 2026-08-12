/**
 * Version-neutral flat resource-to-DTO read fixture catalog: shared annotated DTO models plus
 * scenario expectations consumed by Jackson-major binder contract tests.
 *
 * <p>Types in this package are major-neutral (see ADR-004 and ADR-010): they import only core,
 * annotations, Jackson common, JSpecify, and JDK types — never {@code tools.jackson..} or {@code
 * com.fasterxml.jackson.databind..}. Binder expectations are resource-relative and never read
 * document {@code included} (ADR-011). Write-side models such as {@code BlogWithJsonProperty},
 * {@code Comment}, and {@code Person} remain in {@code domainwrite}.
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}; null-bearing DTO
 * members carry {@link org.jspecify.annotations.Nullable} to preserve omitted and explicit-null
 * states the scenarios exercise.
 */
@NullMarked
package io.github.kazemek.jsonapi.testfixtures.domainread;

import org.jspecify.annotations.NullMarked;
