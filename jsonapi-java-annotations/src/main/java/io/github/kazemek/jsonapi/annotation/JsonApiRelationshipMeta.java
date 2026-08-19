package io.github.kazemek.jsonapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a domain property as the complete JSON:API {@code meta} object of a specific mapped
 * relationship, without requiring a relationship-envelope property.
 *
 * <p>{@link #value()} is required and has no default: it references the target relationship's
 * resolved JSON:API member (wire) name — the value of {@link JsonApiRelationship#name()} when set,
 * otherwise the relationship property's logical Java name. A renamed relationship therefore
 * requires the wire name here. The target relationship must be declared by an {@link
 * JsonApiRelationship} on the same domain mapping; mapping meta for an otherwise undeclared
 * relationship is not supported.
 *
 * <p>At most one {@code @JsonApiRelationshipMeta} property may target a given relationship, and
 * each such property represents the complete {@code meta} object of that relationship's location.
 * Jackson mapping owns target-shape validation, conversion, and diagnostics.
 *
 * <p>The required {@code value()} element is an intentional deviation from the optional {@code
 * name()} convention of {@link JsonApiAttribute} and {@link JsonApiRelationship}: the relationship
 * target is mandatory and no implicit name derivation exists.
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
public @interface JsonApiRelationshipMeta {

  /**
   * The target relationship's resolved JSON:API member (wire) name.
   *
   * @return non-empty relationship member name; emptiness and member-name grammar are validated
   *     when a Jackson mapping definition is built
   */
  String value();
}
