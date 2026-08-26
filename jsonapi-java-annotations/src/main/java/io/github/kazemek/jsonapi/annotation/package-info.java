/**
 * Runtime-visible JSON:API domain-mapping annotations with no functional third-party runtime
 * dependencies.
 *
 * <p>These annotations declare resource type, identifier, attribute-name, relationship-name,
 * resource-meta, relationship-meta, and identifier-meta roles for later Jackson mapping. They store
 * metadata only: they do not discover logical properties, validate member names, convert
 * identifiers, or request inclusion.
 *
 * <p>See ADR-004, ADR-005, ADR-007, ADR-009, and {@code docs/conformance.md}.
 */
@NullMarked
package io.github.kazemek.jsonapi.annotation;

import org.jspecify.annotations.NullMarked;
