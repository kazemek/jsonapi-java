package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson3.RelationshipLinkageMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

/**
 * Shared relationship linkage rules for flat DTO binding and presence-aware PATCH: cardinality
 * checks, target-class resolution, built-in {@link ResourceIdentifier} maps, and custom linkage
 * mappers.
 */
final class RelationshipLinkageSupport {

  private RelationshipLinkageSupport() {}

  static Class<?> resolveTargetClass(
      JavaType propertyType, boolean toMany, MappingProperty property) {
    if (toMany) {
      JavaType contentType = DomainResourceWriter.resolveContentType(propertyType);
      if (contentType == null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET,
            rawTypeOf(property),
            relationshipPath(property),
            "Cannot resolve collection content type for relationship '"
                + property.logicalName()
                + "'");
      }
      return contentType.getRawClass();
    }
    return unwrapOptionalType(propertyType).getRawClass();
  }

  /**
   * Validates linkage shape against the property's cardinality, throwing {@link
   * MappingDiagnostic#RELATIONSHIP_CARDINALITY_MISMATCH} for illegal combinations. Returns whether
   * the linkage denotes an empty value ({@code null} on to-one, empty collection on to-many).
   */
  static boolean validateCardinality(
      MappingProperty property, RelationshipData data, boolean toMany) {
    return switch (data) {
      case RelationshipData.NullLinkage ignored -> {
        if (toMany) {
          throw cardinalityMismatch(property, "null linkage on to-many relationship");
        }
        yield true;
      }
      case RelationshipData.SingleLinkage ignored -> {
        if (toMany) {
          throw cardinalityMismatch(property, "single linkage on to-many relationship");
        }
        yield false;
      }
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers) -> {
        boolean empty = identifiers.isEmpty();
        if (!toMany) {
          throw cardinalityMismatch(
              property,
              empty
                  ? "empty collection linkage on to-one relationship"
                  : "collection linkage on to-one relationship");
        }
        yield empty;
      }
    };
  }

  static @Nullable Object builtInLinkage(
      MappingProperty property, RelationshipData data, boolean toMany) {
    boolean empty = validateCardinality(property, data, toMany);
    return switch (data) {
      case RelationshipData.NullLinkage ignored -> null;
      case RelationshipData.SingleLinkage(ResourceIdentifier identifier) -> linkageMap(identifier);
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers) -> {
        if (empty) {
          yield List.of();
        }
        List<Object> values = new ArrayList<>(identifiers.size());
        for (ResourceIdentifier identifier : identifiers) {
          values.add(linkageMap(identifier));
        }
        yield values;
      }
    };
  }

  static @Nullable Object mappedLinkage(
      MappingProperty property,
      RelationshipData data,
      boolean toMany,
      RelationshipLinkageMapper linkageMapper,
      JavaType mapperTargetType) {
    boolean empty = validateCardinality(property, data, toMany);
    return switch (data) {
      case RelationshipData.NullLinkage ignored -> null;
      case RelationshipData.SingleLinkage single ->
          invokeLinkageMapper(linkageMapper, single, mapperTargetType, property);
      case RelationshipData.IdentifierCollectionLinkage collection -> {
        if (empty) {
          yield List.of();
        }
        yield invokeLinkageMapper(linkageMapper, collection, mapperTargetType, property);
      }
    };
  }

  static @Nullable Object invokeLinkageMapper(
      RelationshipLinkageMapper linkageMapper,
      RelationshipData data,
      JavaType targetType,
      MappingProperty property) {
    try {
      return linkageMapper.map(data, targetType);
    } catch (RuntimeException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.LINKAGE_MAPPING_FAILED,
          rawTypeOf(property),
          relationshipPath(property),
          "Relationship linkage mapper failed for relationship '" + property.logicalName() + "'",
          e);
    }
  }

  static Map<String, @Nullable Object> linkageMap(ResourceIdentifier identifier) {
    Map<String, @Nullable Object> linkage = new LinkedHashMap<>();
    linkage.put("type", identifier.type());
    linkage.put("id", identifier.id());
    linkage.put("lid", identifier.lid());
    return linkage;
  }

  static JavaType unwrapOptionalType(JavaType type) {
    if (type.isTypeOrSubTypeOf(Optional.class) && type.containedTypeCount() == 1) {
      return type.containedType(0);
    }
    return type;
  }

  static Class<?> rawTypeOf(MappingProperty property) {
    return property.accessor().getType().getRawClass();
  }

  static JsonApiMappingException unsupportedRelationshipTarget(
      MappingProperty property, Class<?> targetClass) {
    return new JsonApiMappingException(
        MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_TARGET,
        rawTypeOf(property),
        relationshipPath(property),
        "Relationship '"
            + property.logicalName()
            + "' targets unsupported type "
            + targetClass.getName());
  }

  static String relationshipPath(MappingProperty property) {
    return "/relationships/" + property.jsonapiName() + "/data";
  }

  private static JsonApiMappingException cardinalityMismatch(
      MappingProperty property, String detail) {
    return new JsonApiMappingException(
        MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
        rawTypeOf(property),
        relationshipPath(property),
        "Cardinality mismatch for relationship '" + property.logicalName() + "': " + detail);
  }
}
