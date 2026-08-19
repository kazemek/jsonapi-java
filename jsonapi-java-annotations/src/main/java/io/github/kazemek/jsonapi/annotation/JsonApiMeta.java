package io.github.kazemek.jsonapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a domain property as the complete JSON:API resource-side {@code meta} object.
 *
 * <p>The annotated property represents the whole {@code meta} object of the mapped resource as one
 * application-owned value (a record, POJO, {@code Map}, or {@code Object}); it is not a JSON:API
 * transport wrapper and carries no resource-envelope semantics. Jackson mapping owns target-shape
 * validation, conversion, and diagnostics.
 *
 * <p>At most one {@code @JsonApiMeta} property is allowed per mapped resource. Resource-side meta
 * is distinct from document-level meta (owned by the document envelope) and from relationship meta
 * (owned by {@link JsonApiRelationshipMeta}); no annotation ambiguously means document meta.
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
public @interface JsonApiMeta {}
