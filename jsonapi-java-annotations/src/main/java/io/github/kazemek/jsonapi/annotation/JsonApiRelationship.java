package io.github.kazemek.jsonapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a domain property as JSON:API relationship linkage and optionally names it.
 *
 * <p>This annotation identifies linkage and its name only. It does not request inclusion and
 * carries no fetch, cascade, repository, or ORM policy (ADR-005). Relationship properties cease to
 * be default attributes. Annotations never make Jackson-ignored properties visible.
 *
 * <p>An empty {@link #name()} retains Jackson's logical property name; a non-empty override is
 * interpreted and validated only by Phase 2.2, which also owns cardinality, value-shape, and
 * logical-property conflict diagnostics.
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
public @interface JsonApiRelationship {

  /**
   * Optional JSON:API relationship field-name override.
   *
   * <p>The empty string is the explicit “use Jackson's logical property name” sentinel, never Java
   * {@code null}.
   *
   * @return field-name override, or {@code ""} to keep Jackson's logical name
   */
  String name() default "";
}
