/**
 * Version-neutral compound-inclusion write fixture catalog: shared annotated graph builders plus
 * {@link CompoundWriteScenario} / {@link CompoundWriteScenarios} consumed by Jackson-major compound
 * serialization contract tests.
 *
 * <p>Types in this package are major-neutral (see ADR-004 and ADR-010): they import only
 * annotations, Jackson common, {@code domainwrite} models, JSpecify, and JDK types — never {@code
 * tools.jackson..} or {@code com.fasterxml.jackson.databind..}. Expected included resources are
 * ordered identity refs, never serialized text. Absent {@code included} is {@code null}; a present
 * empty array is an empty list.
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}; null-bearing model
 * members such as {@code LinkedArticle.related}, {@code DeepNode.child}, {@code CyclicNode.child},
 * and {@code BaseComment} fields carry {@link org.jspecify.annotations.Nullable} to preserve the
 * intentional null states the scenarios exercise.
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.compoundwrite;

import org.jspecify.annotations.NullMarked;
