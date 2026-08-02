package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson3.IdentifierConverter;
import io.github.kazemek.jsonapi.jackson3.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson3.MappingDiagnostic;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

public final class DomainResourceWriter {

  private final JsonMapper mapper;
  private final IdentifierConverter identifierConverter;
  private final MappingDefinitionCache cache;

  public DomainResourceWriter(
      JsonMapper mapper, IdentifierConverter identifierConverter, MappingDefinitionCache cache) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.identifierConverter = Objects.requireNonNull(identifierConverter, "identifierConverter");
    this.cache = Objects.requireNonNull(cache, "cache");
  }

  public ResourceObject toResource(Object resource) {
    Objects.requireNonNull(resource, "resource");
    ResourceMapping mapping = cache.resolve(resource.getClass());
    String id = extractId(resource, mapping);
    Attributes attributes = buildAttributes(resource, mapping);
    Relationships relationships = buildRelationships(resource, mapping);
    return new ResourceObject(
        mapping.resourceType(),
        id,
        null,
        attributes.isEmpty() ? null : attributes,
        relationships.isEmpty() ? null : relationships,
        null,
        null,
        Map.of());
  }

  @Nullable String extractId(Object resource, ResourceMapping mapping) {
    MappingProperty identifierProperty = mapping.identifierProperty();
    if (identifierProperty == null) {
      return null;
    }
    Object identifierValue =
        unwrapOptional(readValue(resource, identifierProperty, PropertyRole.ID));
    if (identifierValue == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_IDENTIFIER,
          resource.getClass(),
          identifierProperty.logicalName(),
          "Identifier property '" + identifierProperty.logicalName() + "' is null");
    }
    String identifierString = identifierConverter.convert(identifierValue);
    if (identifierString == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_IDENTIFIER,
          resource.getClass(),
          identifierProperty.logicalName(),
          "Identifier converter returned null for property '"
              + identifierProperty.logicalName()
              + "'");
    }
    return identifierString;
  }

  public ResourceIdentifier extractIdentifier(Object resource) {
    Objects.requireNonNull(resource, "resource");
    Class<?> rawType = resource.getClass();
    ResourceMapping mapping = cache.resolve(rawType);
    String id = extractId(resource, mapping);
    return new ResourceIdentifier(mapping.resourceType(), id, null, null, Map.of());
  }

  private Attributes buildAttributes(Object resource, ResourceMapping mapping) {
    if (mapping.attributes().isEmpty()) {
      return Attributes.empty();
    }
    Map<String, Object> attributes = new LinkedHashMap<>();
    for (MappingProperty property : mapping.attributes()) {
      Object rawValue = readValue(resource, property, PropertyRole.ATTRIBUTE);
      if (rawValue instanceof Optional<?> optional && optional.isEmpty()) {
        continue;
      }
      Object value = unwrapOptional(rawValue);
      attributes.put(property.jsonapiName(), convertAttributeValue(value));
    }
    return Attributes.ofAttributes(attributes);
  }

  private Relationships buildRelationships(Object resource, ResourceMapping mapping) {
    if (mapping.relationships().isEmpty()) {
      return Relationships.empty();
    }
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    for (MappingProperty property : mapping.relationships()) {
      relationships.put(property.jsonapiName(), buildRelationship(resource, property));
    }
    return Relationships.ofRelationships(relationships);
  }

  private Relationship buildRelationship(Object resource, MappingProperty property) {
    Object value = readValue(resource, property, PropertyRole.RELATIONSHIP);
    JavaType propertyType = property.accessor().getType();
    boolean isToMany = isToManyType(propertyType);

    if (isToMany) {
      RelationshipData linkage = extractToManyLinkage(value, propertyType);
      return new Relationship(linkage, null, null, Map.of());
    }
    RelationshipData linkage = extractToOneLinkage(value);
    return new Relationship(linkage, null, null, Map.of());
  }

  private RelationshipData extractToOneLinkage(@Nullable Object value) {
    value = unwrapOptional(value);
    if (value == null) {
      return RelationshipData.NullLinkage.INSTANCE;
    }
    if (value instanceof ResourceIdentifier resourceIdentifier) {
      return new RelationshipData.SingleLinkage(resourceIdentifier);
    }
    if (value instanceof RelationshipData relationshipData) {
      return relationshipData;
    }
    ResourceIdentifier identifier = extractIdentifier(value);
    return new RelationshipData.SingleLinkage(identifier);
  }

  private RelationshipData extractToManyLinkage(@Nullable Object value, JavaType propType) {
    if (value == null) {
      return RelationshipData.IdentifierCollectionLinkage.empty();
    }
    List<?> items = convertToCollection(value);
    if (items.isEmpty()) {
      return RelationshipData.IdentifierCollectionLinkage.empty();
    }
    boolean hasResourceIdentifier = false;
    Object firstNonResourceIdentifier = null;
    for (Object item : items) {
      if (item == null) {
        continue;
      }
      if (item instanceof ResourceIdentifier) {
        hasResourceIdentifier = true;
      } else if (firstNonResourceIdentifier == null) {
        firstNonResourceIdentifier = item;
      }
    }
    if (hasResourceIdentifier && firstNonResourceIdentifier != null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
          firstNonResourceIdentifier.getClass(),
          null,
          "Mixed element types in to-many relationship collection: expected ResourceIdentifier, got "
              + firstNonResourceIdentifier.getClass().getName());
    }
    if (hasResourceIdentifier) {
      List<ResourceIdentifier> identifiers = new ArrayList<>();
      for (Object item : items) {
        if (item == null) {
          continue;
        }
        identifiers.add((ResourceIdentifier) item);
      }
      return new RelationshipData.IdentifierCollectionLinkage(identifiers);
    }
    JavaType contentType = resolveContentType(propType);
    if (contentType == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
          null,
          null,
          "Cannot resolve collection content type");
    }
    Class<?> elementClass = contentType.getRawClass();
    checkResourceAnnotation(elementClass);
    List<ResourceIdentifier> identifiers = new ArrayList<>();
    for (Object item : items) {
      if (item == null) {
        continue;
      }
      identifiers.add(extractIdentifier(item));
    }
    return new RelationshipData.IdentifierCollectionLinkage(identifiers);
  }

  private static @Nullable Object readValue(
      Object resource, MappingProperty property, PropertyRole role) {
    try {
      return property.accessor().getValue(resource);
    } catch (JsonApiMappingException e) {
      throw e;
    } catch (Exception e) {
      throw new JsonApiMappingException(
          diagnosticFor(role),
          resource.getClass(),
          property.logicalName(),
          "Failed to read property '"
              + property.logicalName()
              + "' ("
              + role.name().toLowerCase()
              + ")",
          e);
    }
  }

  private static MappingDiagnostic diagnosticFor(PropertyRole role) {
    return switch (role) {
      case ID -> MappingDiagnostic.MISSING_IDENTIFIER;
      case ATTRIBUTE -> MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE;
      case RELATIONSHIP -> MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE;
    };
  }

  private @Nullable Object convertAttributeValue(@Nullable Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return value;
    }
    return mapper.convertValue(value, Object.class);
  }

  private static List<?> convertToCollection(Object value) {
    if (value instanceof List<?> list) {
      return list;
    }
    if (value instanceof Object[] array) {
      return List.of(array);
    }
    if (value instanceof Iterable<?> iterable) {
      List<Object> result = new ArrayList<>();
      for (Object item : iterable) {
        result.add(item);
      }
      return result;
    }
    throw new JsonApiMappingException(
        MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_VALUE,
        value.getClass(),
        null,
        "To-many relationship value is not a supported collection type: "
            + value.getClass().getName());
  }

  private static boolean isToManyType(JavaType type) {
    if (type.isArrayType()) {
      return true;
    }
    if (type.isCollectionLikeType()) {
      return true;
    }
    return type.isTypeOrSubTypeOf(Iterable.class);
  }

  private static @Nullable JavaType resolveContentType(JavaType type) {
    if (type.isArrayType() || type.isCollectionLikeType()) {
      return type.getContentType();
    }
    if (type.containedTypeCount() > 0) {
      return type.containedType(0);
    }
    return null;
  }

  private static @Nullable Object unwrapOptional(@Nullable Object value) {
    if (value instanceof Optional<?> optional) {
      return optional.orElse(null);
    }
    return value;
  }

  private static void checkResourceAnnotation(Class<?> rawType) {
    if (rawType.getAnnotation(JsonApiResource.class) == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_RELATIONSHIP_COLLECTION_TYPE,
          rawType,
          null,
          "Collection element type " + rawType.getName() + " lacks @JsonApiResource");
    }
  }
}
