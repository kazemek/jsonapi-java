package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.validation.MemberNames;
import io.github.kazemek.jsonapi.jackson3.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson3.MappingDiagnostic;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.BeanPropertyDefinition;

final class MappingDefinitionResolver {

  private MappingDefinitionResolver() {}

  static ResourceMapping resolve(BeanDescription beanDescription, Class<?> rawType) {
    JsonApiResource resourceAnnotation = validateResourceAnnotation(rawType);
    String resourceType = validateResourceType(resourceAnnotation, rawType);

    List<BeanPropertyDefinition> propertyDefinitions = beanDescription.findProperties();
    List<MappingProperty> identifierProperties = new ArrayList<>();
    List<MappingProperty> attributeProperties = new ArrayList<>();
    List<MappingProperty> relationshipProperties = new ArrayList<>();

    classifyProperties(
        propertyDefinitions,
        rawType,
        identifierProperties,
        attributeProperties,
        relationshipProperties);
    validatePropertyRoles(
        identifierProperties, attributeProperties, relationshipProperties, rawType);

    MappingProperty identifier =
        identifierProperties.isEmpty() ? null : identifierProperties.getFirst();
    return new ResourceMapping(
        resourceType,
        identifier,
        List.copyOf(attributeProperties),
        List.copyOf(relationshipProperties),
        beanDescription.getType());
  }

  private static JsonApiResource validateResourceAnnotation(Class<?> rawType) {
    JsonApiResource resourceAnnotation = rawType.getAnnotation(JsonApiResource.class);
    if (resourceAnnotation == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_RESOURCE_ANNOTATION,
          rawType,
          null,
          "Missing @JsonApiResource on " + rawType.getName());
    }
    return resourceAnnotation;
  }

  private static String validateResourceType(JsonApiResource resourceAnnotation, Class<?> rawType) {
    String resourceType = resourceAnnotation.type();
    if (resourceType.isEmpty()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_RESOURCE_TYPE,
          rawType,
          null,
          "@JsonApiResource.type() must not be empty on " + rawType.getName());
    }
    if (!MemberNames.isValid(resourceType)) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_RESOURCE_TYPE,
          rawType,
          null,
          "Invalid resource type name: " + resourceType);
    }
    return resourceType;
  }

  private static void classifyProperties(
      List<BeanPropertyDefinition> propertyDefinitions,
      Class<?> rawType,
      List<MappingProperty> identifierProperties,
      List<MappingProperty> attributeProperties,
      List<MappingProperty> relationshipProperties) {
    for (BeanPropertyDefinition propertyDefinition : propertyDefinitions) {
      AnnotatedMember accessor = propertyDefinition.getAccessor();
      if (accessor == null) {
        continue;
      }
      String logicalName = propertyDefinition.getName();
      PropertyRole role = resolveRole(propertyDefinition, logicalName, rawType);
      String jsonapiName = resolveJsonapiName(propertyDefinition, logicalName, role);

      validateJsonApiName(jsonapiName, role, propertyDefinition, rawType);

      MappingProperty mappingProperty =
          new MappingProperty(propertyDefinition, accessor, logicalName, jsonapiName, role);
      switch (role) {
        case ID -> identifierProperties.add(mappingProperty);
        case ATTRIBUTE -> attributeProperties.add(mappingProperty);
        case RELATIONSHIP -> relationshipProperties.add(mappingProperty);
      }
    }
  }

  private static PropertyRole resolveRole(
      BeanPropertyDefinition propertyDefinition, String logicalName, Class<?> rawType) {
    boolean hasIdentifier = hasAnnotationAnywhere(propertyDefinition, JsonApiId.class);
    boolean hasRelationship = hasAnnotationAnywhere(propertyDefinition, JsonApiRelationship.class);
    boolean hasAttribute = hasAnnotationAnywhere(propertyDefinition, JsonApiAttribute.class);

    int count = (hasIdentifier ? 1 : 0) + (hasRelationship ? 1 : 0) + (hasAttribute ? 1 : 0);
    if (count > 1) {
      throw new JsonApiMappingException(
          MappingDiagnostic.DUPLICATE_ROLE,
          rawType,
          logicalName,
          "Property '" + logicalName + "' has conflicting role annotations");
    }

    if (hasIdentifier) {
      return PropertyRole.ID;
    }
    if (hasRelationship) {
      return PropertyRole.RELATIONSHIP;
    }
    if (hasAttribute) {
      return PropertyRole.ATTRIBUTE;
    }
    if ("id".equals(logicalName)) {
      return PropertyRole.ID;
    }
    return PropertyRole.ATTRIBUTE;
  }

  private static String resolveJsonapiName(
      BeanPropertyDefinition propertyDefinition, String logicalName, PropertyRole role) {
    return switch (role) {
      case ID -> logicalName;
      case ATTRIBUTE -> {
        JsonApiAttribute annotation =
            findAnnotationAnywhere(propertyDefinition, JsonApiAttribute.class);
        yield annotation != null && !annotation.name().isEmpty() ? annotation.name() : logicalName;
      }
      case RELATIONSHIP -> {
        JsonApiRelationship annotation =
            findAnnotationAnywhere(propertyDefinition, JsonApiRelationship.class);
        yield annotation != null && !annotation.name().isEmpty() ? annotation.name() : logicalName;
      }
    };
  }

  private static void validateJsonApiName(
      String jsonapiName,
      PropertyRole role,
      BeanPropertyDefinition propertyDefinition,
      Class<?> rawType) {
    if (!MemberNames.isValid(jsonapiName)) {
      MappingDiagnostic diagnostic =
          switch (role) {
            case ATTRIBUTE -> MappingDiagnostic.INVALID_ATTRIBUTE_NAME;
            case RELATIONSHIP -> MappingDiagnostic.INVALID_RELATIONSHIP_NAME;
            default -> null;
          };
      if (diagnostic != null) {
        throw new JsonApiMappingException(
            diagnostic,
            rawType,
            propertyDefinition.getName(),
            "Invalid JSON:API member name: " + jsonapiName);
      }
    }
  }

  private static void validatePropertyRoles(
      List<MappingProperty> identifierProperties,
      List<MappingProperty> attributeProperties,
      List<MappingProperty> relationshipProperties,
      Class<?> rawType) {
    if (identifierProperties.isEmpty()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_IDENTIFIER,
          rawType,
          null,
          "No identifier property found for " + rawType.getName());
    }
    if (identifierProperties.size() > 1) {
      throw new JsonApiMappingException(
          MappingDiagnostic.DUPLICATE_ROLE,
          rawType,
          null,
          "Multiple identifier properties found for " + rawType.getName());
    }

    Set<String> seen = new HashSet<>();
    for (MappingProperty attribute : attributeProperties) {
      if (!seen.add(attribute.jsonapiName())) {
        throw new JsonApiMappingException(
            MappingDiagnostic.NAME_COLLISION,
            rawType,
            attribute.jsonapiName(),
            "Duplicate attribute name: " + attribute.jsonapiName());
      }
    }
    seen.clear();
    for (MappingProperty relationship : relationshipProperties) {
      if (!seen.add(relationship.jsonapiName())) {
        throw new JsonApiMappingException(
            MappingDiagnostic.NAME_COLLISION,
            rawType,
            relationship.jsonapiName(),
            "Duplicate relationship name: " + relationship.jsonapiName());
      }
    }

    for (MappingProperty attribute : attributeProperties) {
      for (MappingProperty relationship : relationshipProperties) {
        if (attribute.jsonapiName().equals(relationship.jsonapiName())) {
          throw new JsonApiMappingException(
              MappingDiagnostic.NAME_COLLISION,
              rawType,
              attribute.jsonapiName(),
              "Attribute and relationship name collision: " + attribute.jsonapiName());
        }
      }
    }
  }

  private static boolean hasAnnotationAnywhere(
      BeanPropertyDefinition propertyDefinition, Class<? extends Annotation> annotationClass) {
    return findAnnotationAnywhere(propertyDefinition, annotationClass) != null;
  }

  private static <A extends Annotation> @Nullable A findAnnotationAnywhere(
      BeanPropertyDefinition propertyDefinition, Class<A> annotationClass) {
    for (AnnotatedMember member :
        new AnnotatedMember[] {
          propertyDefinition.getField(),
          propertyDefinition.getGetter(),
          propertyDefinition.getSetter(),
          propertyDefinition.getConstructorParameter()
        }) {
      if (member != null) {
        A annotation = member.getAnnotation(annotationClass);
        if (annotation != null) {
          return annotation;
        }
      }
    }
    return null;
  }
}
