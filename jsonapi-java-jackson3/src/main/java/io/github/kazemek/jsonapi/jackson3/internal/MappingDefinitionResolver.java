package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.JsonApiMembers;
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
      AnnotatedMember accessor = requireAccessorIfAnnotated(propertyDefinition, rawType);
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

  private static @Nullable AnnotatedMember requireAccessorIfAnnotated(
      BeanPropertyDefinition propertyDefinition, Class<?> rawType) {
    AnnotatedMember accessor = propertyDefinition.getAccessor();
    if (accessor != null) {
      return accessor;
    }
    if (RoleAnnotations.from(propertyDefinition).hasAny()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_ACCESSOR,
          rawType,
          propertyDefinition.getName(),
          "Annotated property '" + propertyDefinition.getName() + "' has no readable accessor");
    }
    return null;
  }

  private static PropertyRole resolveRole(
      BeanPropertyDefinition propertyDefinition, String logicalName, Class<?> rawType) {
    RoleAnnotations annotations = RoleAnnotations.from(propertyDefinition);
    if (annotations.count() > 1) {
      throw new JsonApiMappingException(
          MappingDiagnostic.DUPLICATE_ROLE,
          rawType,
          logicalName,
          "Property '" + logicalName + "' has conflicting role annotations");
    }
    PropertyRole explicitRole = annotations.explicitRole();
    if (explicitRole != null) {
      return explicitRole;
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
    MappingDiagnostic diagnostic =
        switch (role) {
          case ATTRIBUTE -> MappingDiagnostic.INVALID_ATTRIBUTE_NAME;
          case RELATIONSHIP -> MappingDiagnostic.INVALID_RELATIONSHIP_NAME;
          default -> null;
        };
    if (diagnostic == null) {
      return;
    }
    if (isForbiddenMemberName(jsonapiName)) {
      throw new JsonApiMappingException(
          diagnostic,
          rawType,
          propertyDefinition.getName(),
          "Invalid JSON:API member name: " + jsonapiName);
    }
  }

  private static boolean isForbiddenMemberName(String jsonapiName) {
    return !MemberNames.isValid(jsonapiName)
        || JsonApiMembers.ID.equals(jsonapiName)
        || JsonApiMembers.TYPE.equals(jsonapiName);
  }

  private static void validatePropertyRoles(
      List<MappingProperty> identifierProperties,
      List<MappingProperty> attributeProperties,
      List<MappingProperty> relationshipProperties,
      Class<?> rawType) {
    requireSingleIdentifier(identifierProperties, rawType);
    rejectDuplicateNames(attributeProperties, rawType, "attribute");
    rejectDuplicateNames(relationshipProperties, rawType, "relationship");
    rejectAttributeRelationshipCollisions(attributeProperties, relationshipProperties, rawType);
  }

  private static void requireSingleIdentifier(
      List<MappingProperty> identifierProperties, Class<?> rawType) {
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
  }

  private static void rejectDuplicateNames(
      List<MappingProperty> properties, Class<?> rawType, String roleLabel) {
    Set<String> seen = new HashSet<>();
    for (MappingProperty property : properties) {
      if (!seen.add(property.jsonapiName())) {
        throw new JsonApiMappingException(
            MappingDiagnostic.NAME_COLLISION,
            rawType,
            property.jsonapiName(),
            "Duplicate " + roleLabel + " name: " + property.jsonapiName());
      }
    }
  }

  private static void rejectAttributeRelationshipCollisions(
      List<MappingProperty> attributeProperties,
      List<MappingProperty> relationshipProperties,
      Class<?> rawType) {
    Set<String> relationshipNames = new HashSet<>();
    for (MappingProperty relationship : relationshipProperties) {
      relationshipNames.add(relationship.jsonapiName());
    }
    for (MappingProperty attribute : attributeProperties) {
      if (relationshipNames.contains(attribute.jsonapiName())) {
        throw new JsonApiMappingException(
            MappingDiagnostic.NAME_COLLISION,
            rawType,
            attribute.jsonapiName(),
            "Attribute and relationship name collision: " + attribute.jsonapiName());
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
      // Jackson's getAnnotation is not @Nullable-annotated; use hasAnnotation as the presence
      // check.
      if (member != null && member.hasAnnotation(annotationClass)) {
        return member.getAnnotation(annotationClass);
      }
    }
    return null;
  }

  private record RoleAnnotations(boolean id, boolean attribute, boolean relationship) {

    static RoleAnnotations from(BeanPropertyDefinition propertyDefinition) {
      return new RoleAnnotations(
          hasAnnotationAnywhere(propertyDefinition, JsonApiId.class),
          hasAnnotationAnywhere(propertyDefinition, JsonApiAttribute.class),
          hasAnnotationAnywhere(propertyDefinition, JsonApiRelationship.class));
    }

    int count() {
      return (id ? 1 : 0) + (attribute ? 1 : 0) + (relationship ? 1 : 0);
    }

    boolean hasAny() {
      return count() > 0;
    }

    @Nullable PropertyRole explicitRole() {
      if (id) {
        return PropertyRole.ID;
      }
      if (relationship) {
        return PropertyRole.RELATIONSHIP;
      }
      if (attribute) {
        return PropertyRole.ATTRIBUTE;
      }
      return null;
    }
  }
}
