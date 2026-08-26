package io.github.kazemek.jsonapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a domain property as the application-owned {@code meta} of JSON:API resource identifiers
 * used as relationship linkage, without requiring a relationship-envelope property.
 *
 * <p>This is identifier-level meta ({@code ResourceIdentifier.meta}), not relationship-level meta
 * ({@link JsonApiRelationshipMeta}). For a to-one relationship the property is one complete meta
 * object. For a to-many relationship the property is an order-preserving sequence of complete meta
 * objects aligned with the linkage identifiers; it is not relationship meta and is not a generic
 * list-element PATCH address.
 *
 * <p>{@link #value()} is required and has no default: it references the target relationship's
 * resolved JSON:API member (wire) name — the value of {@link JsonApiRelationship#name()} when set,
 * otherwise the relationship property's logical Java name. A renamed relationship therefore
 * requires the wire name here. The target relationship must be declared by an {@link
 * JsonApiRelationship} on the same domain mapping; mapping identifier meta for an otherwise
 * undeclared relationship is not supported.
 *
 * <p>At most one {@code @JsonApiIdentifierMeta} property may target a given relationship. Jackson
 * mapping owns target-shape validation, conversion, and diagnostics. Identifier meta is not
 * independently patchable; it participates in presence-aware PATCH only as part of whole-linkage
 * replacement.
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
public @interface JsonApiIdentifierMeta {

  /**
   * The target relationship's resolved JSON:API member (wire) name.
   *
   * @return non-empty relationship member name; emptiness, member-name grammar, and cardinality
   *     alignment are validated when a Jackson mapping definition is built
   */
  String value();
}
