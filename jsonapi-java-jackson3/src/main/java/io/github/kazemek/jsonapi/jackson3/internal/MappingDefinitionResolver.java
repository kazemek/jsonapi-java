package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.JsonApiMembers;
import io.github.kazemek.jsonapi.core.validation.MemberNames;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.introspect.BeanPropertyDefinition;

final class MappingDefinitionResolver {

  private MappingDefinitionResolver() {}

  static ResourceMapping resolve(
      BeanDescription beanDescription, Class<?> rawType, AnnotatedClass resourceMetadata) {
    String resourceType = validateResourceTypeName(resourceTypeName(resourceMetadata), rawType);

    List<BeanPropertyDefinition> propertyDefinitions = beanDescription.findProperties();
    List<MappingProperty> identifierProperties = new ArrayList<>();
    List<MappingProperty> attributeProperties = new ArrayList<>();
    List<MappingProperty> relationshipProperties = new ArrayList<>();
    List<MappingProperty> resourceMetaProperties = new ArrayList<>();
    List<MappingProperty> relationshipMetaProperties = new ArrayList<>();

    classifyProperties(
        propertyDefinitions,
        rawType,
        identifierProperties,
        attributeProperties,
        relationshipProperties,
        resourceMetaProperties,
        relationshipMetaProperties);
    validatePropertyRoles(
        identifierProperties,
        attributeProperties,
        relationshipProperties,
        resourceMetaProperties,
        relationshipMetaProperties,
        rawType);

    MappingProperty identifier =
        identifierProperties.isEmpty() ? null : identifierProperties.getFirst();
    MappingProperty resourceMeta =
        resourceMetaProperties.isEmpty() ? null : resourceMetaProperties.getFirst();
    return new ResourceMapping(
        resourceType,
        identifier,
        List.copyOf(attributeProperties),
        List.copyOf(relationshipProperties),
        resourceMeta,
        List.copyOf(relationshipMetaProperties),
        beanDescription.getType());
  }

  /**
   * Reads the configured class-level resource type name from the mapper-introspected direct class
   * annotations, or {@code null} when absent. This is the single interpretation of class-level
   * {@link JsonApiResource} metadata: the direct {@link AnnotatedClass} carries the configured
   * mapper's view of the target class, including a target-specific class-level mix-in, without
   * importing annotations from supertypes or interfaces.
   */
  static @Nullable String resourceTypeName(AnnotatedClass annotatedClass) {
    // Jackson's getAnnotation is not @Nullable-annotated; use hasAnnotation as the presence check.
    if (!annotatedClass.hasAnnotation(JsonApiResource.class)) {
      return null;
    }
    return annotatedClass.getAnnotation(JsonApiResource.class).type();
  }

  /**
   * Validates a class-level resource type name resolved by {@link #resourceTypeName}.
   *
   * @throws JsonApiMappingException {@link MappingDiagnostic#MISSING_RESOURCE_ANNOTATION} when the
   *     name is absent, or {@link MappingDiagnostic#INVALID_RESOURCE_TYPE} when it is empty or not
   *     a valid JSON:API member name
   */
  static String validateResourceTypeName(@Nullable String resourceTypeName, Class<?> rawType) {
    if (resourceTypeName == null) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_RESOURCE_ANNOTATION,
          rawType,
          null,
          "Missing @JsonApiResource on " + rawType.getName());
    }
    if (resourceTypeName.isEmpty()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_RESOURCE_TYPE,
          rawType,
          null,
          "@JsonApiResource.type() must not be empty on " + rawType.getName());
    }
    if (!MemberNames.isValid(resourceTypeName)) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_RESOURCE_TYPE,
          rawType,
          null,
          "Invalid resource type name: " + resourceTypeName);
    }
    return resourceTypeName;
  }

  private static void classifyProperties(
      List<BeanPropertyDefinition> propertyDefinitions,
      Class<?> rawType,
      List<MappingProperty> identifierProperties,
      List<MappingProperty> attributeProperties,
      List<MappingProperty> relationshipProperties,
      List<MappingProperty> resourceMetaProperties,
      List<MappingProperty> relationshipMetaProperties) {
    for (BeanPropertyDefinition propertyDefinition : propertyDefinitions) {
      RoleAnnotations annotations = RoleAnnotations.from(propertyDefinition);
      AnnotatedMember accessor =
          requireAccessorIfAnnotated(propertyDefinition, rawType, annotations);
      if (accessor == null) {
        continue;
      }
      String logicalName = propertyDefinition.getName();
      PropertyRole role = resolveRole(annotations, logicalName, rawType);
      String jsonapiName = resolveJsonapiName(annotations, logicalName, role);
      validateJsonApiName(jsonapiName, role, propertyDefinition, rawType);

      MappingProperty mappingProperty =
          new MappingProperty(propertyDefinition, accessor, logicalName, jsonapiName, role);
      switch (role) {
        case ID -> identifierProperties.add(mappingProperty);
        case ATTRIBUTE -> attributeProperties.add(mappingProperty);
        case RELATIONSHIP -> relationshipProperties.add(mappingProperty);
        case RESOURCE_META -> resourceMetaProperties.add(mappingProperty);
        case RELATIONSHIP_META -> relationshipMetaProperties.add(mappingProperty);
      }
    }
  }

  private static @Nullable AnnotatedMember requireAccessorIfAnnotated(
      BeanPropertyDefinition propertyDefinition, Class<?> rawType, RoleAnnotations annotations) {
    AnnotatedMember accessor = propertyDefinition.getAccessor();
    if (accessor != null) {
      return accessor;
    }
    if (annotations.hasAny()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_ACCESSOR,
          rawType,
          propertyDefinition.getName(),
          "Annotated property '" + propertyDefinition.getName() + "' has no readable accessor");
    }
    return null;
  }

  private static PropertyRole resolveRole(
      RoleAnnotations annotations, String logicalName, Class<?> rawType) {
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
      RoleAnnotations annotations, String logicalName, PropertyRole role) {
    return switch (role) {
      case ID -> logicalName;
      case ATTRIBUTE -> {
        JsonApiAttribute annotation = annotations.attribute();
        yield annotation != null && !annotation.name().isEmpty() ? annotation.name() : logicalName;
      }
      case RELATIONSHIP -> {
        JsonApiRelationship annotation = annotations.relationship();
        yield annotation != null && !annotation.name().isEmpty() ? annotation.name() : logicalName;
      }
      case RESOURCE_META -> JsonApiMembers.META;
      case RELATIONSHIP_META -> {
        JsonApiRelationshipMeta annotation = annotations.relationshipMeta();
        yield Objects.requireNonNull(annotation).value();
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
          case RELATIONSHIP_META -> MappingDiagnostic.INVALID_RELATIONSHIP_META_TARGET;
          default -> null;
        };
    if (diagnostic == null) {
      return;
    }
    if (jsonapiName.isEmpty() || isForbiddenMemberName(jsonapiName)) {
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
      List<MappingProperty> resourceMetaProperties,
      List<MappingProperty> relationshipMetaProperties,
      Class<?> rawType) {
    requireSingleIdentifier(identifierProperties, rawType);
    rejectDuplicateNames(attributeProperties, rawType, "attribute");
    rejectDuplicateNames(relationshipProperties, rawType, "relationship");
    rejectAttributeRelationshipCollisions(attributeProperties, relationshipProperties, rawType);
    requireSingleResourceMeta(resourceMetaProperties, rawType);
    validateRelationshipMetaTargets(relationshipMetaProperties, relationshipProperties, rawType);
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

  private static void requireSingleResourceMeta(
      List<MappingProperty> resourceMetaProperties, Class<?> rawType) {
    if (resourceMetaProperties.size() > 1) {
      throw new JsonApiMappingException(
          MappingDiagnostic.DUPLICATE_ROLE,
          rawType,
          resourceMetaProperties.get(1).logicalName(),
          "Multiple resource meta properties found for "
              + rawType.getName()
              + "; at most one @JsonApiMeta property is allowed");
    }
  }

  private static void validateRelationshipMetaTargets(
      List<MappingProperty> relationshipMetaProperties,
      List<MappingProperty> relationshipProperties,
      Class<?> rawType) {
    if (relationshipMetaProperties.isEmpty()) {
      return;
    }
    Set<String> relationshipNames = new HashSet<>();
    for (MappingProperty relationship : relationshipProperties) {
      relationshipNames.add(relationship.jsonapiName());
    }
    Set<String> seen = new HashSet<>();
    for (MappingProperty property : relationshipMetaProperties) {
      String target = property.jsonapiName();
      if (!relationshipNames.contains(target)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.UNRESOLVED_RELATIONSHIP_META,
            rawType,
            property.logicalName(),
            "@JsonApiRelationshipMeta references unknown relationship '"
                + target
                + "' on "
                + rawType.getName());
      }
      if (!seen.add(target)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.DUPLICATE_ROLE,
            rawType,
            property.logicalName(),
            "Multiple relationship meta properties target relationship '"
                + target
                + "' on "
                + rawType.getName()
                + "; at most one is allowed");
      }
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

  private record RoleAnnotations(
      @Nullable JsonApiId id,
      @Nullable JsonApiAttribute attribute,
      @Nullable JsonApiRelationship relationship,
      @Nullable JsonApiMeta meta,
      @Nullable JsonApiRelationshipMeta relationshipMeta) {

    static RoleAnnotations from(BeanPropertyDefinition propertyDefinition) {
      AnnotatedMember[] members = {
        propertyDefinition.getField(),
        propertyDefinition.getGetter(),
        propertyDefinition.getSetter(),
        propertyDefinition.getConstructorParameter()
      };
      return new RoleAnnotations(
          findAnnotationAnywhere(members, JsonApiId.class),
          findAnnotationAnywhere(members, JsonApiAttribute.class),
          findAnnotationAnywhere(members, JsonApiRelationship.class),
          findAnnotationAnywhere(members, JsonApiMeta.class),
          findAnnotationAnywhere(members, JsonApiRelationshipMeta.class));
    }

    private static <A extends Annotation> @Nullable A findAnnotationAnywhere(
        AnnotatedMember[] members, Class<A> annotationClass) {
      for (AnnotatedMember member : members) {
        // Jackson's getAnnotation is not @Nullable-annotated; use hasAnnotation as the presence
        // check.
        if (member != null && member.hasAnnotation(annotationClass)) {
          return member.getAnnotation(annotationClass);
        }
      }
      return null;
    }

    int count() {
      return (id != null ? 1 : 0)
          + (attribute != null ? 1 : 0)
          + (relationship != null ? 1 : 0)
          + (meta != null ? 1 : 0)
          + (relationshipMeta != null ? 1 : 0);
    }

    boolean hasAny() {
      return count() > 0;
    }

    @Nullable PropertyRole explicitRole() {
      if (id != null) {
        return PropertyRole.ID;
      }
      if (relationship != null) {
        return PropertyRole.RELATIONSHIP;
      }
      if (attribute != null) {
        return PropertyRole.ATTRIBUTE;
      }
      if (meta != null) {
        return PropertyRole.RESOURCE_META;
      }
      if (relationshipMeta != null) {
        return PropertyRole.RELATIONSHIP_META;
      }
      return null;
    }
  }
}
