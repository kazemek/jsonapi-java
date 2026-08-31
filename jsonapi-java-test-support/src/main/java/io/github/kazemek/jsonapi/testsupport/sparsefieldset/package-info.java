/**
 * Version-neutral sparse-fieldset write fixture catalog: shared annotated models plus the {@link
 * SparseFieldsetScenarios} catalog and the {@link SparseFieldsetOperation} / {@link
 * SparseFieldsetRequest} / {@link SparseFieldsetExpectation} value types consumed by Jackson-major
 * sparse-fieldset contract tests. {@link FieldsetResourceState#assertMatches} is the shared
 * resource-state comparator, including resource-level meta.
 *
 * <p>Types in this package are major-neutral (see ADR-004 and ADR-010): they import only
 * annotations, Jackson common, {@code domainwrite} models, JSpecify, and JDK types — never {@code
 * tools.jackson..} or {@code com.fasterxml.jackson.databind..}. Expected resource states pin
 * ordered surviving field names and version-neutral core linkage, never serialized text. Absent
 * attributes, relationships, and {@code included} are {@code null}; a present empty {@code
 * included} array is an empty list.
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}; null-bearing
 * expectation members such as absent attributes/relationships, absent {@code included}, and
 * rejection {@code resourceClass}/{@code propertyPath} carry {@link
 * org.jspecify.annotations.Nullable}.
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.sparsefieldset;

import org.jspecify.annotations.NullMarked;
