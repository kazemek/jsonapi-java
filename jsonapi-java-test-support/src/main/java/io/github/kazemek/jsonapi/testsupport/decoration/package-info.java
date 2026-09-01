/**
 * Shared decoration contract catalog and verifier for JSON:API resource-link decoration.
 *
 * <p>Cross-major scenarios assert that decoration adds only {@code ResourceObject.links} and mapped
 * {@code Relationship.links}, preserves linkage/meta/attributes, applies to primary and
 * compound-included resources with the same semantics, and never resurrects fieldset-omitted
 * relationships. Jackson-major suites execute the catalog through {@link
 * io.github.kazemek.jsonapi.testsupport.decoration.DecorationVerifier} so Jackson 2 can reuse the
 * identical contract without copying assertions.
 */
@NullMarked
package io.github.kazemek.jsonapi.testsupport.decoration;

import org.jspecify.annotations.NullMarked;
