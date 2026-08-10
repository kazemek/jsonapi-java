package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson3.RelationshipLinkageMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.exc.MismatchedInputException;
import tools.jackson.databind.exc.ValueInstantiationException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Binds validated {@link ResourceObject} values to annotated flat DTO types.
 *
 * <p>Identifier, attribute, and relationship values are placed into a synthetic property map keyed
 * by Jackson logical property names, then the bean is constructed with a single {@link
 * JsonMapper#convertValue(Object, JavaType)} so creators, deserializers, converters, and configured
 * modules remain authoritative (ADR-004). Document {@code included} is never read; relationships
 * bind from linkage only (ADR-011).
 */
public final class DomainResourceBinder {

  private static final String IDENTIFIER_PATH_ID = "/id";
  private static final String IDENTIFIER_PATH_LID = "/lid";

  private final JsonMapper mapper;
  private final IdentifierConverter identifierConverter;
  private final MappingDefinitionCache cache;
  private final Map<Class<?>, RelationshipLinkageMapper> linkageMappers;

  public DomainResourceBinder(
      JsonMapper mapper,
      IdentifierConverter identifierConverter,
      MappingDefinitionCache cache,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.identifierConverter = Objects.requireNonNull(identifierConverter, "identifierConverter");
    this.cache = Objects.requireNonNull(cache, "cache");
    this.linkageMappers = Map.copyOf(Objects.requireNonNull(linkageMappers, "linkageMappers"));
  }

  /** Binds one resource object to the given target type. */
  public Object fromResource(ResourceObject resource, JavaType targetType) {
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(targetType, "targetType");
    Class<?> rawType = targetType.getRawClass();
    ResourceMapping mapping = cache.resolve(rawType);
    validateResourceType(resource, mapping, rawType);
    Map<String, @Nullable Object> properties = new LinkedHashMap<>();
    bindIdentifier(resource, mapping, properties);
    bindAttributes(resource, mapping, properties);
    bindRelationships(resource, mapping, properties);
    return convertBean(properties, targetType, rawType);
  }

  private void validateResourceType(
      ResourceObject resource, ResourceMapping mapping, Class<?> rawType) {
    String expectedType = mapping.resourceType();
    if (!expectedType.equals(resource.type())) {
      throw new JsonApiMappingException(
          MappingDiagnostic.RESOURCE_TYPE_MISMATCH,
          rawType,
          "/type",
          "Resource object type '"
              + resource.type()
              + "' does not match expected type '"
              + expectedType
              + "'");
    }
  }

  private void bindIdentifier(
      ResourceObject resource, ResourceMapping mapping, Map<String, @Nullable Object> properties) {
    MappingProperty identifierProperty = mapping.identifierProperty();
    if (identifierProperty == null) {
      return;
    }
    if (resource.hasId()) {
      bindIdentifierValue(
          Objects.requireNonNull(resource.id()),
          IDENTIFIER_PATH_ID,
          identifierProperty,
          properties);
      return;
    }
    String localIdentifier = resource.lid();
    if (localIdentifier != null) {
      bindIdentifierValue(localIdentifier, IDENTIFIER_PATH_LID, identifierProperty, properties);
    }
  }

  private void bindIdentifierValue(
      String wireIdentifier,
      String identifierPath,
      MappingProperty identifierProperty,
      Map<String, @Nullable Object> properties) {
    Object parsed;
    try {
      parsed = identifierConverter.parse(wireIdentifier);
    } catch (RuntimeException e) {
      throw identifierConversionFailed(rawTypeOf(identifierProperty), identifierPath, e);
    }
    if (parsed == null) {
      throw identifierConversionFailed(rawTypeOf(identifierProperty), identifierPath, null);
    }
    Object converted;
    try {
      converted = mapper.convertValue(parsed, identifierProperty.accessor().getType());
    } catch (RuntimeException e) {
      throw identifierConversionFailed(rawTypeOf(identifierProperty), identifierPath, e);
    }
    properties.put(identifierProperty.logicalName(), converted);
  }

  private JsonApiMappingException identifierConversionFailed(
      Class<?> rawType, String identifierPath, @Nullable Throwable cause) {
    String message =
        cause == null
            ? "Identifier converter returned null for the wire identifier at '"
                + identifierPath
                + "'"
            : "Failed to convert the wire identifier at '"
                + identifierPath
                + "' for "
                + rawType.getName();
    return cause == null
        ? new JsonApiMappingException(
            MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, rawType, identifierPath, message)
        : new JsonApiMappingException(
            MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED,
            rawType,
            identifierPath,
            message,
            cause);
  }

  private static Class<?> rawTypeOf(MappingProperty property) {
    return property.accessor().getType().getRawClass();
  }

  private void bindAttributes(
      ResourceObject resource, ResourceMapping mapping, Map<String, @Nullable Object> properties) {
    if (mapping.attributes().isEmpty()) {
      return;
    }
    Attributes attributes = resource.attributes();
    if (attributes == null) {
      return;
    }
    Map<String, @Nullable Object> members = attributes.attributes();
    for (MappingProperty property : mapping.attributes()) {
      if (!members.containsKey(property.jsonapiName())) {
        continue;
      }
      properties.put(property.logicalName(), members.get(property.jsonapiName()));
    }
  }

  private void bindRelationships(
      ResourceObject resource, ResourceMapping mapping, Map<String, @Nullable Object> properties) {
    if (mapping.relationships().isEmpty()) {
      return;
    }
    Relationships relationships = resource.relationships();
    if (relationships == null) {
      return;
    }
    for (MappingProperty property : mapping.relationships()) {
      RelationshipData data = relationshipData(relationships, property);
      if (data == null) {
        continue;
      }
      bindRelationship(properties, property, data);
    }
  }

  private static @Nullable RelationshipData relationshipData(
      Relationships relationships, MappingProperty property) {
    Relationship relationship = relationships.relationships().get(property.jsonapiName());
    if (relationship == null) {
      return null;
    }
    return relationship.data();
  }

  private void bindRelationship(
      Map<String, @Nullable Object> properties, MappingProperty property, RelationshipData data) {
    JavaType propertyType = property.accessor().getType();
    boolean toMany = DomainResourceWriter.isToManyType(propertyType);
    Class<?> targetClass = resolveTargetClass(propertyType, toMany, property);
    if (targetClass == ResourceIdentifier.class) {
      putBuiltInLinkage(properties, property, data, toMany);
      return;
    }
    RelationshipLinkageMapper linkageMapper = linkageMappers.get(targetClass);
    if (linkageMapper == null) {
      throw unsupportedRelationshipTarget(property, targetClass);
    }
    JavaType mapperTargetType = toMany ? propertyType : unwrapOptionalType(propertyType);
    putMappedLinkage(properties, property, data, toMany, linkageMapper, mapperTargetType);
  }

  private static Class<?> resolveTargetClass(
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

  private void putBuiltInLinkage(
      Map<String, @Nullable Object> properties,
      MappingProperty property,
      RelationshipData data,
      boolean toMany) {
    boolean empty = validateCardinality(property, data, toMany);
    switch (data) {
      case RelationshipData.NullLinkage ignored -> properties.put(property.logicalName(), null);
      case RelationshipData.SingleLinkage(ResourceIdentifier identifier) ->
          properties.put(property.logicalName(), linkageMap(identifier));
      case RelationshipData.IdentifierCollectionLinkage(List<ResourceIdentifier> identifiers) -> {
        if (empty) {
          properties.put(property.logicalName(), List.of());
          return;
        }
        List<Object> values = new ArrayList<>(identifiers.size());
        for (ResourceIdentifier identifier : identifiers) {
          values.add(linkageMap(identifier));
        }
        properties.put(property.logicalName(), values);
      }
    }
  }

  private void putMappedLinkage(
      Map<String, @Nullable Object> properties,
      MappingProperty property,
      RelationshipData data,
      boolean toMany,
      RelationshipLinkageMapper linkageMapper,
      JavaType propertyType) {
    boolean empty = validateCardinality(property, data, toMany);
    switch (data) {
      case RelationshipData.NullLinkage ignored -> properties.put(property.logicalName(), null);
      case RelationshipData.SingleLinkage single ->
          properties.put(
              property.logicalName(),
              invokeLinkageMapper(linkageMapper, single, propertyType, property));
      case RelationshipData.IdentifierCollectionLinkage collection -> {
        if (empty) {
          properties.put(property.logicalName(), List.of());
          return;
        }
        properties.put(
            property.logicalName(),
            invokeLinkageMapper(linkageMapper, collection, propertyType, property));
      }
    }
  }

  /**
   * Validates linkage shape against the property's cardinality, throwing {@link
   * MappingDiagnostic#RELATIONSHIP_CARDINALITY_MISMATCH} for illegal combinations. Returns whether
   * the linkage denotes an empty value ({@code null} on to-one, empty collection on to-many).
   */
  private static boolean validateCardinality(
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

  private static @Nullable Object invokeLinkageMapper(
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

  private static Map<String, @Nullable Object> linkageMap(ResourceIdentifier identifier) {
    Map<String, @Nullable Object> linkage = new LinkedHashMap<>();
    linkage.put("type", identifier.type());
    linkage.put("id", identifier.id());
    linkage.put("lid", identifier.lid());
    return linkage;
  }

  private Object convertBean(
      Map<String, @Nullable Object> properties, JavaType targetType, Class<?> rawType) {
    try {
      return mapper.convertValue(properties, targetType);
    } catch (RuntimeException e) {
      Throwable failure = jacksonFailure(e);
      MappingDiagnostic diagnostic =
          isCreatorInputFailure(failure)
              ? MappingDiagnostic.MISSING_CREATOR_INPUT
              : MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE;
      throw new JsonApiMappingException(
          diagnostic,
          rawType,
          propertyPath(failure),
          "Failed to construct " + rawType.getName() + " from resource values",
          failure);
    }
  }

  /**
   * Classifies bulk {@code convertValue} failures as creator/instantiation input problems.
   *
   * <p>Jackson 3.2 reports missing creator properties as {@link MismatchedInputException} with the
   * "Missing required creator property" message, and creator/instantiation failures (including
   * throwing creators) as {@link ValueInstantiationException}. Both mean the bean could not be
   * constructed from the supplied inputs, so both map to {@link
   * MappingDiagnostic#MISSING_CREATOR_INPUT}; all other coercion, type, or property failures map to
   * {@link MappingDiagnostic#UNSUPPORTED_ATTRIBUTE_VALUE} (milestone Phase 2.9 contract).
   */
  private static boolean isCreatorInputFailure(Throwable failure) {
    if (failure instanceof ValueInstantiationException) {
      return true;
    }
    if (failure instanceof MismatchedInputException mismatched) {
      String message = mismatched.getMessage();
      return message != null && message.contains("Missing required creator property");
    }
    return false;
  }

  private static Throwable jacksonFailure(Throwable failure) {
    Throwable current = failure;
    while (current != null) {
      if (current instanceof JacksonException) {
        return current;
      }
      current = current.getCause();
    }
    return failure;
  }

  private static String propertyPath(Throwable failure) {
    if (failure instanceof JacksonException jackson) {
      List<JacksonException.Reference> path = jackson.getPath();
      if (path != null) {
        for (int i = path.size() - 1; i >= 0; i--) {
          String name = path.get(i).getPropertyName();
          if (name != null && !name.isEmpty()) {
            return "/" + name;
          }
        }
      }
    }
    return "/";
  }

  private static JavaType unwrapOptionalType(JavaType type) {
    if (type.isTypeOrSubTypeOf(Optional.class) && type.containedTypeCount() == 1) {
      return type.containedType(0);
    }
    return type;
  }

  private static JsonApiMappingException cardinalityMismatch(
      MappingProperty property, String detail) {
    return new JsonApiMappingException(
        MappingDiagnostic.RELATIONSHIP_CARDINALITY_MISMATCH,
        rawTypeOf(property),
        relationshipPath(property),
        "Cardinality mismatch for relationship '" + property.logicalName() + "': " + detail);
  }

  private static JsonApiMappingException unsupportedRelationshipTarget(
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

  private static String relationshipPath(MappingProperty property) {
    return "/relationships/" + property.jsonapiName() + "/data";
  }
}
