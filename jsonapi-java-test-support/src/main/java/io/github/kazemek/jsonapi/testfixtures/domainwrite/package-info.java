/**
 * Version-neutral flat domain-to-resource write fixture catalog: shared annotated domain models
 * plus scenario expectations consumed by Jackson-major contract tests.
 *
 * <p>Types in this package are major-neutral (see ADR-004 and ADR-010): they import only core,
 * annotations, Jackson common, JSpecify, and JDK types — never {@code tools.jackson..} or {@code
 * com.fasterxml.jackson.databind..}. Expected outcomes are core/common values ({@link
 * io.github.kazemek.jsonapi.core.model.ResourceObject}, {@link
 * io.github.kazemek.jsonapi.core.model.JsonApiDocument}, linkage variants), never serialized text.
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}; null-bearing model
 * members such as {@code Article.author} and {@code Person.name} carry {@link
 * org.jspecify.annotations.Nullable} to preserve the intentional null states the scenarios
 * exercise.
 */
@NullMarked
package io.github.kazemek.jsonapi.testfixtures.domainwrite;

import org.jspecify.annotations.NullMarked;
