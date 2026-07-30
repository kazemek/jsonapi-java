package io.github.kazemek.jsonapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares that a domain type maps to a JSON:API resource of the given {@link #type()}.
 *
 * <p>{@code type()} is required and has no default. Member-name grammar validation for the type
 * string is performed when Phase 2.2 builds a mapping definition, not by this annotation artifact.
 *
 * <p>{@code ElementType.TYPE} permits classes, records, interfaces, enums, and annotation types at
 * compile time. Supported domain shapes and diagnostics for unsupported placements are defined by
 * Phase 2.2.
 *
 * <p>This annotation is not {@link java.lang.annotation.Inherited}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JsonApiResource {

  /**
   * JSON:API resource type member value.
   *
   * @return non-empty type string; emptiness and member-name grammar are validated in Phase 2.2
   */
  String type();
}
