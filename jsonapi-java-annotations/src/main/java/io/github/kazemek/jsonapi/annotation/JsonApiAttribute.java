package io.github.kazemek.jsonapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optionally overrides the JSON:API attribute field name for a domain property.
 *
 * <p>Jackson remains authoritative for logical property discovery, visibility, names, values, and
 * serialization. An empty {@link #name()} retains Jackson's logical property name; a non-empty
 * override is interpreted and validated (including member-name grammar) only by Phase 2.2.
 *
 * <p>Annotations never make Jackson-ignored properties visible. Identifier and relationship roles
 * cease to be default attributes; remaining Jackson-visible properties become attributes by default
 * after those roles are applied (ADR-004).
 *
 * <p>Targets include fields, methods, parameters, and record components. Phase 2.2 collapses
 * propagated occurrences into one logical property.
 *
 * <p>This annotation is not {@link java.lang.annotation.Inherited}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({
  ElementType.FIELD,
  ElementType.METHOD,
  ElementType.PARAMETER,
  ElementType.RECORD_COMPONENT
})
public @interface JsonApiAttribute {

  /**
   * Optional JSON:API attribute field-name override.
   *
   * <p>The empty string is the explicit “use Jackson's logical property name” sentinel, never Java
   * {@code null}.
   *
   * @return field-name override, or {@code ""} to keep Jackson's logical name
   */
  String name() default "";
}
