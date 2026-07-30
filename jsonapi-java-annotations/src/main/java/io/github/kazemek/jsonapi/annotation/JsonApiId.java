package io.github.kazemek.jsonapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks the JSON:API resource identifier on a domain property.
 *
 * <p>An explicit {@code @JsonApiId}, or Phase 2.2's conventional logical property named {@code id},
 * supplies the identifier. Identifier properties cease to be default attributes. Annotations never
 * make Jackson-ignored properties visible; Phase 2.2 resolves visibility through Jackson's logical
 * property model and owns identifier conversion and conflict diagnostics.
 *
 * <p>Targets include fields, methods, parameters, and record components so Java and Jackson can
 * expose the same logical property. Phase 2.2 collapses propagated occurrences into one logical
 * property rather than inventing field/getter precedence.
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
public @interface JsonApiId {}
