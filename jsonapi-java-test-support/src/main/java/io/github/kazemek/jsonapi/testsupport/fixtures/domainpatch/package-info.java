/**
 * Passive, application-shaped PATCH fixture carriers shared across Jackson-major adapter suites:
 * ordinary structured values, presence-aware PATCH DTOs, JavaBean-style mutable shapes, and the
 * intentionally invalid declarations exercised by declaration-validation scenarios.
 *
 * <p>This package is coverage-exempt by placement: only passive carriers may live here. Scenario
 * catalogs, scenario/expectation descriptors, resource loading, invariants, and other executable
 * test-support logic belong outside {@code testsupport.fixtures..} (see the module
 * README). @NullMarked
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch;

import org.jspecify.annotations.NullMarked;
