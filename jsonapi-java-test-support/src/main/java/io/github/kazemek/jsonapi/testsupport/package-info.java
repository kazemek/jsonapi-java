/**
 * Version-neutral shared test-support contracts: the {@link Scenario} descriptor contract, the
 * {@link FixtureCatalog} views it feeds, and {@link TestSupportResources} classpath loading.
 *
 * <p>Each feature catalog class (for example {@code codec.CodecScenarios} or {@code
 * domainpatch.PatchScenarios}) owns exactly one {@link FixtureCatalog} and exposes it through a
 * single static {@code catalog()} accessor; that accessor is the canonical retrieval path for
 * adapter suites. Passive application-shaped carriers live only under {@code
 * io.github.kazemek.jsonapi.testsupport.fixtures..}; executable catalogs, descriptors, loaders, and
 * invariants stay outside that hierarchy (see the module README for the ownership and coverage
 * model).
 *
 * <p>Per ADR-009 this package is {@link org.jspecify.annotations.NullMarked}.
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport;

import org.jspecify.annotations.NullMarked;
