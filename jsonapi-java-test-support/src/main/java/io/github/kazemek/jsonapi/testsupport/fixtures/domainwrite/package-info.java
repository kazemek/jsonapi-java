/**
 * Passive, application-shaped domain-write fixture carriers shared across Jackson-major adapter
 * suites. These types model an ordinary application's binding surface: annotations, records or
 * beans, accessors and mutators, and simple value equality.
 *
 * <p>This package is coverage-exempt by placement: only passive carriers may live here. Scenario
 * catalogs, descriptors, resource loading, invariants, and other executable test-support logic
 * belong outside {@code testsupport.fixtures..} (see the module README). @NullMarked
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import org.jspecify.annotations.NullMarked;
