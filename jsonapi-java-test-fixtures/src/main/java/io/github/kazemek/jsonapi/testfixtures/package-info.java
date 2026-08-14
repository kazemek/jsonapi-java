/**
 * Version-neutral shared test-fixture contracts: {@link Scenario}, {@link FixtureCatalog}, the
 * {@link JsonApiFixtures} retrieval facade, and {@link FixtureDirectory}.
 *
 * <p>{@link JsonApiFixtures} plus the {@link FixtureCatalog} instances it exposes is the canonical
 * retrieval API for adapter suites and future catalogs. Concrete {@code *Scenarios} classes live in
 * {@code codec}, {@code domainwrite}, {@code domainread}, {@code compoundwrite}, {@code
 * sparsefieldset}, and {@code enveloperead} and retain static shims for existing consumers.
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}.
 */
@NullMarked
package io.github.kazemek.jsonapi.testfixtures;

import org.jspecify.annotations.NullMarked;
