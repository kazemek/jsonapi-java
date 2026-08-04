package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.RelationshipData;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

/**
 * Converts JSON:API relationship linkage into a value for a flat DTO relationship property.
 *
 * <p>Register implementations by target class with {@link
 * JsonApiJackson3#resourceBinder(tools.jackson.databind.json.JsonMapper, IdentifierConverter,
 * java.util.Map)} when a relationship property's target type is not one of the built-in {@link
 * io.github.kazemek.jsonapi.core.model.ResourceIdentifier} shapes. The binder invokes the mapper
 * only for {@link RelationshipData.SingleLinkage} (to-one properties) and non-empty {@link
 * RelationshipData.IdentifierCollectionLinkage} (to-many properties); explicit null and empty
 * linkage short-circuit without a mapper call, and to-one versus to-many cardinality is enforced
 * before invocation.
 *
 * <p>{@code targetType} is the property's Java type with {@link java.util.Optional} unwrapped (a
 * collection type for to-many properties). The returned value is placed directly into the binder's
 * synthetic property map, so it must be coercible to {@code targetType}; a {@code null} return
 * binds the property to {@code null}.
 */
@FunctionalInterface
public interface RelationshipLinkageMapper {

  /**
   * Converts linkage to a property value, or returns {@code null} for an empty value.
   *
   * @throws RuntimeException when the linkage cannot be converted; the binder reports this as
   *     {@link MappingDiagnostic#LINKAGE_MAPPING_FAILED}
   */
  @Nullable Object map(RelationshipData linkage, JavaType targetType);
}
