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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
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
    return RelationshipLinkageSupport.rawTypeOf(property);
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
    Class<?> targetClass =
        RelationshipLinkageSupport.resolveTargetClass(propertyType, toMany, property);
    if (targetClass == ResourceIdentifier.class) {
      properties.put(
          property.logicalName(),
          RelationshipLinkageSupport.builtInLinkage(property, data, toMany));
      return;
    }
    RelationshipLinkageMapper linkageMapper = linkageMappers.get(targetClass);
    if (linkageMapper == null) {
      throw RelationshipLinkageSupport.unsupportedRelationshipTarget(property, targetClass);
    }
    JavaType mapperTargetType =
        toMany ? propertyType : RelationshipLinkageSupport.unwrapOptionalType(propertyType);
    properties.put(
        property.logicalName(),
        RelationshipLinkageSupport.mappedLinkage(
            property, data, toMany, linkageMapper, mapperTargetType));
  }

  private Object convertBean(
      Map<String, @Nullable Object> properties, JavaType targetType, Class<?> rawType) {
    return BeanConstruction.convertBean(mapper, properties, targetType, rawType);
  }
}
