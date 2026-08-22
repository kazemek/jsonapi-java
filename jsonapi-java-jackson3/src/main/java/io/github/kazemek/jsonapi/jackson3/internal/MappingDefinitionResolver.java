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
import io.github.kazemek.jsonapi.jackson.MappingLocation;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
      throw JsonApiMappingException.withoutLocation(
          MappingDiagnostic.MISSING_RESOURCE_ANNOTATION,
          rawType,
          "Missing @JsonApiResource on " + rawType.getName());
    }
    if (resourceTypeName.isEmpty()) {
      throw JsonApiMappingException.withoutLocation(
          MappingDiagnostic.INVALID_RESOURCE_TYPE,
          rawType,
          "@JsonApiResource.type() must not be empty on " + rawType.getName());
    }
    if (!MemberNames.isValid(resourceTypeName)) {
      throw JsonApiMappingException.withoutLocation(
          MappingDiagnostic.INVALID_RESOURCE_TYPE,
          rawType,
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
      String logicalName = propertyDefinition.getName();
      // Role and wire name resolve from annotations alone so member-level declaration failures can
      // report the member's resource-relative wire location even when the accessor is missing.
      PropertyRole role = resolveRole(annotations, logicalName, rawType);
      String jsonapiName = resolveJsonapiName(annotations, logicalName, role);
      validateJsonApiName(jsonapiName, role, logicalName, rawType);
      AnnotatedMember accessor =
          requireAccessorIfAnnotated(
              propertyDefinition,
              logicalName,
              rawType,
              annotations,
              wireLocation(role, jsonapiName));
      if (accessor == null) {
        continue;
      }

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

  /**
   * Resource-relative wire location of one classified member: {@code /id}, {@code
   * /attributes/<name>}, {@code /relationships/<name>/data}, {@code /meta}, or {@code
   * /relationships/<name>/meta}. The name is escaped as pointer segments per RFC 6901.
   */
  private static MappingLocation wireLocation(PropertyRole role, String jsonapiName) {
    return switch (role) {
      case ID -> MappingLocation.of("id");
      case ATTRIBUTE -> attributeLocation(jsonapiName);
      case RELATIONSHIP -> relationshipLocation(jsonapiName);
      case RESOURCE_META -> RelationshipMetaSupport.resourceMetaLocation();
      case RELATIONSHIP_META -> RelationshipMetaSupport.relationshipMetaLocation(jsonapiName);
    };
  }

  private static @Nullable AnnotatedMember requireAccessorIfAnnotated(
      BeanPropertyDefinition propertyDefinition,
      String logicalName,
      Class<?> rawType,
      RoleAnnotations annotations,
      @Nullable MappingLocation memberLocation) {
    AnnotatedMember accessor = propertyDefinition.getAccessor();
    if (accessor != null) {
      return accessor;
    }
    if (annotations.hasAny()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.MISSING_ACCESSOR,
          rawType,
          memberLocation,
          "Annotated property '" + logicalName + "' has no readable accessor");
    }
    return null;
  }

  private static PropertyRole resolveRole(
      RoleAnnotations annotations, String logicalName, Class<?> rawType) {
    if (annotations.count() > 1) {
      // Conflicting role annotations leave no single wire member to point at; the property stays
      // identified in the message per the mapping-location contract.
      throw JsonApiMappingException.withoutLocation(
          MappingDiagnostic.DUPLICATE_ROLE,
          rawType,
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
      String jsonapiName, PropertyRole role, String logicalName, Class<?> rawType) {
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
      // The wire name itself is invalid, so it cannot form a pointer segment; the offending name
      // and its logical property stay in the message.
      throw JsonApiMappingException.withoutLocation(
          diagnostic,
          rawType,
          "Invalid JSON:API member name '" + jsonapiName + "' for property '" + logicalName + "'");
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
    rejectDuplicateNames(
        attributeProperties, rawType, "attribute", MappingDefinitionResolver::attributeLocation);
    rejectDuplicateNames(
        relationshipProperties,
        rawType,
        "relationship",
        MappingDefinitionResolver::relationshipLocation);
    rejectAttributeRelationshipCollisions(attributeProperties, relationshipProperties, rawType);
    requireSingleResourceMeta(resourceMetaProperties, rawType);
    validateRelationshipMetaTargets(relationshipMetaProperties, relationshipProperties, rawType);
  }

  private static void requireSingleIdentifier(
      List<MappingProperty> identifierProperties, Class<?> rawType) {
    if (identifierProperties.isEmpty()) {
      throw JsonApiMappingException.withoutLocation(
          MappingDiagnostic.MISSING_IDENTIFIER,
          rawType,
          "No identifier property found for " + rawType.getName());
    }
    if (identifierProperties.size() > 1) {
      throw JsonApiMappingException.withoutLocation(
          MappingDiagnostic.DUPLICATE_ROLE,
          rawType,
          "Multiple identifier properties found for " + rawType.getName());
    }
  }

  private static void requireSingleResourceMeta(
      List<MappingProperty> resourceMetaProperties, Class<?> rawType) {
    if (resourceMetaProperties.size() > 1) {
      throw new JsonApiMappingException(
          MappingDiagnostic.DUPLICATE_ROLE,
          rawType,
          RelationshipMetaSupport.resourceMetaLocation(),
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
            RelationshipMetaSupport.relationshipMetaLocation(target),
            "@JsonApiRelationshipMeta for property '"
                + property.logicalName()
                + "' references unknown relationship '"
                + target
                + "' on "
                + rawType.getName());
      }
      if (!seen.add(target)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.DUPLICATE_ROLE,
            rawType,
            RelationshipMetaSupport.relationshipMetaLocation(target),
            "Multiple relationship meta properties target relationship '"
                + target
                + "' on "
                + rawType.getName()
                + "; at most one is allowed");
      }
    }
  }

  private static void rejectDuplicateNames(
      List<MappingProperty> properties,
      Class<?> rawType,
      String roleLabel,
      Function<String, MappingLocation> containerLocation) {
    Set<String> seen = new HashSet<>();
    for (MappingProperty property : properties) {
      if (!seen.add(property.jsonapiName())) {
        throw new JsonApiMappingException(
            MappingDiagnostic.NAME_COLLISION,
            rawType,
            containerLocation.apply(property.jsonapiName()),
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
        // The colliding name could live under either container; no single member location applies.
        throw JsonApiMappingException.withoutLocation(
            MappingDiagnostic.NAME_COLLISION,
            rawType,
            "Attribute and relationship name collision: " + attribute.jsonapiName());
      }
    }
  }

  private static MappingLocation attributeLocation(String jsonapiName) {
    return MappingLocation.of("attributes", jsonapiName);
  }

  private static MappingLocation relationshipLocation(String jsonapiName) {
    return MappingLocation.of("relationships", jsonapiName, "data");
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
