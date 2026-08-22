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
import io.github.kazemek.jsonapi.jackson.MappingLocation;
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
 * modules remain authoritative (ADR-004). The JSON:API identifier is parsed before it enters the
 * map, so its target property's configured deserializer still applies during construction. Document
 * {@code included} is never read; relationships bind from linkage only (ADR-011).
 *
 * <p>Diagnostic locations follow the shared mapping-location contract: resource-relative pointers
 * over wire names ({@code /type}, {@code /id}, {@code /lid}, {@code /attributes/<name>}, {@code
 * /relationships/<name>/data}, meta locations). Bean-construction failures translate their Jackson
 * failure paths through this mapping; unmappable paths carry an absent location instead of a
 * logical property name.
 */
public final class DomainResourceBinder {

  private static final MappingLocation TYPE_LOCATION = MappingLocation.of("type");
  private static final MappingLocation ID_LOCATION = MappingLocation.of("id");
  private static final MappingLocation LID_LOCATION = MappingLocation.of("lid");

  private final JsonMapper mapper;
  private final IdentifierConverter identifierConverter;
  private final MappingDefinitionCache cache;
  private final Map<Class<?>, RelationshipLinkageMapper> linkageMappers;
  private final WholeMetaTarget wholeMetaTarget;
  private final StructuredValueBinder structuredBinder;

  public DomainResourceBinder(
      JsonMapper mapper,
      IdentifierConverter identifierConverter,
      MappingDefinitionCache cache,
      Map<Class<?>, RelationshipLinkageMapper> linkageMappers) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.identifierConverter = Objects.requireNonNull(identifierConverter, "identifierConverter");
    this.cache = Objects.requireNonNull(cache, "cache");
    this.linkageMappers = Map.copyOf(Objects.requireNonNull(linkageMappers, "linkageMappers"));
    this.wholeMetaTarget = new WholeMetaTarget(mapper);
    this.structuredBinder = new StructuredValueBinder(mapper);
  }

  /** Binds one resource object to the given target type. */
  public Object fromResource(ResourceObject resource, JavaType targetType) {
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(targetType, "targetType");
    Class<?> rawType = targetType.getRawClass();
    ResourceMapping mapping = cache.resolve(targetType);
    validateResourceType(resource, mapping, rawType);
    validateMetaTargets(mapping, rawType);
    Map<String, @Nullable Object> properties = new LinkedHashMap<>();
    MappingLocation identifierLocation = null;
    MappingProperty identifierProperty = mapping.identifierProperty();
    if (identifierProperty != null) {
      if (resource.hasId()) {
        identifierLocation = ID_LOCATION;
        bindIdentifierValue(
            Objects.requireNonNull(resource.id()), ID_LOCATION, identifierProperty, properties);
      } else if (resource.lid() != null) {
        identifierLocation = LID_LOCATION;
        bindIdentifierValue(resource.lid(), LID_LOCATION, identifierProperty, properties);
      }
    }
    bindAttributes(resource, mapping, properties);
    bindRelationships(resource, mapping, properties);
    bindResourceMeta(resource, mapping, properties);
    bindRelationshipMeta(resource, mapping, properties);
    return convertBean(properties, targetType, rawType, mapping, identifierLocation);
  }

  private void validateResourceType(
      ResourceObject resource, ResourceMapping mapping, Class<?> rawType) {
    String expectedType = mapping.resourceType();
    if (!expectedType.equals(resource.type())) {
      throw new JsonApiMappingException(
          MappingDiagnostic.RESOURCE_TYPE_MISMATCH,
          rawType,
          TYPE_LOCATION,
          "Resource object type '"
              + resource.type()
              + "' does not match expected type '"
              + expectedType
              + "'");
    }
  }

  /** Whole-meta declared-target validation for the read/write domain-mapping role (ADR-015). */
  private void validateMetaTargets(ResourceMapping mapping, Class<?> rawType) {
    wholeMetaTarget.validateReadWriteTargets(mapping, rawType);
  }

  private void bindIdentifierValue(
      String wireIdentifier,
      MappingLocation identifierLocation,
      MappingProperty identifierProperty,
      Map<String, @Nullable Object> properties) {
    Object parsed;
    try {
      parsed = identifierConverter.parse(wireIdentifier);
    } catch (RuntimeException e) {
      throw identifierConversionFailed(rawTypeOf(identifierProperty), identifierLocation, e);
    }
    if (parsed == null) {
      throw identifierConversionFailed(rawTypeOf(identifierProperty), identifierLocation, null);
    }
    // Keep the parsed JSON:API intermediate in the synthetic property map. The final bean
    // construction then applies the target property's fully contextualized Jackson deserializer
    // exactly once, rather than converting the detached identifier as a root value first.
    properties.put(identifierProperty.logicalName(), parsed);
  }

  private JsonApiMappingException identifierConversionFailed(
      Class<?> rawType, MappingLocation identifierLocation, @Nullable Throwable cause) {
    String message =
        cause == null
            ? "Identifier converter returned null for the wire identifier at '"
                + identifierLocation
                + "'"
            : "Failed to convert the wire identifier at '"
                + identifierLocation
                + "' for "
                + rawType.getName();
    return cause == null
        ? new JsonApiMappingException(
            MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED, rawType, identifierLocation, message)
        : new JsonApiMappingException(
            MappingDiagnostic.IDENTIFIER_CONVERSION_FAILED,
            rawType,
            identifierLocation,
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

  /**
   * Binds the resource-side {@code meta} members under the mapped resource-meta property's logical
   * name when the resource carries meta. Absent meta leaves the property absent.
   */
  private void bindResourceMeta(
      ResourceObject resource, ResourceMapping mapping, Map<String, @Nullable Object> properties) {
    MappingProperty resourceMetaProperty = mapping.resourceMeta();
    if (resourceMetaProperty == null || resource.meta() == null) {
      return;
    }
    properties.put(resourceMetaProperty.logicalName(), resource.meta().members());
  }

  /**
   * Binds relationship {@code meta} members under each mapped relationship-meta property's logical
   * name when the referenced relationship is present and carries meta. Absent relationship or
   * absent meta leaves the property absent. A valid meta-only relationship representation binds its
   * meta here (read side); PATCH additionally requires {@code data} (ADR-015).
   */
  private void bindRelationshipMeta(
      ResourceObject resource, ResourceMapping mapping, Map<String, @Nullable Object> properties) {
    if (mapping.relationshipMetaProperties().isEmpty()) {
      return;
    }
    Relationships relationships = resource.relationships();
    if (relationships == null) {
      return;
    }
    for (MappingProperty property : mapping.relationshipMetaProperties()) {
      Relationship relationship = relationships.relationships().get(property.jsonapiName());
      if (relationship == null || relationship.meta() == null) {
        continue;
      }
      properties.put(property.logicalName(), relationship.meta().members());
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
      Map<String, @Nullable Object> properties,
      JavaType targetType,
      Class<?> rawType,
      ResourceMapping mapping,
      @Nullable MappingLocation identifierLocation) {
    Map<String, StructuredValueBinder.ConstructionStart> startsByLogicalName =
        mapping.constructionStartsByLogicalName(identifierLocation);
    try {
      return BeanConstruction.convertBean(
          mapper,
          properties,
          targetType,
          rawType,
          (failure, ignored) ->
              structuredBinder.translateConstructionPath(
                  BeanConstruction.pathNames(failure), startsByLogicalName, true));
    } catch (JsonApiMappingException e) {
      MappingProperty identifierProperty = mapping.identifierProperty();
      if (identifierProperty != null
          && identifierLocation != null
          && BeanConstruction.isConstructionFailureForProperty(
              e, identifierProperty, identifierLocation)) {
        Throwable cause = e.getCause() == null ? e : e.getCause();
        throw identifierConversionFailed(rawType, identifierLocation, cause);
      }
      throw e;
    }
  }
}
