/**
 * Passive, application-shaped flat-read DTO fixture carriers shared across Jackson-major adapter
 * suites: records, mutable beans, creator-based shapes, inheritance, meta-bearing DTOs, and
 * instrumented/throwing variants used by binding-failure scenarios.
 *
 * <p>This package is coverage-exempt by placement: only passive carriers may live here. Scenario
 * catalogs, input/expectation descriptors, resource loading, invariants, and other executable
 * test-support logic belong outside {@code testsupport.fixtures..} (see the module
 * README). @NullMarked
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import org.jspecify.annotations.NullMarked;
