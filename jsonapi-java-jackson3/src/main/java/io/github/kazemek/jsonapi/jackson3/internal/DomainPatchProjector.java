package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchChange;
import io.github.kazemek.jsonapi.jackson.PatchCommand;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.util.ClassUtil;

/**
 * Projects a {@link PatchCommand} into an application-owned patch DTO without re-reading JSON or
 * mutating domain state.
 *
 * <p>Each patchable property on the target type must be exactly {@link PatchPresence}{@code <T>};
 * supplied changes are matched by JSON:API member identity ({@code PropertyRole} + {@code
 * jsonapiName}), and changes outside the target surface fail explicitly.
 */
public final class DomainPatchProjector {

  private final MappingDefinitionCache cache;

  public DomainPatchProjector(MappingDefinitionCache cache) {
    this.cache = Objects.requireNonNull(cache, "cache");
  }

  /** Projects one patch command into the given patch DTO type. */
  public Object project(PatchCommand<?> command, JavaType patchType) {
    Objects.requireNonNull(command, "command");
    Objects.requireNonNull(patchType, "patchType");
    Class<?> patchRawType = patchType.getRawClass();
    ResourceMapping patchMapping = cache.resolvePatch(patchType);
    ResourceMapping commandMapping = cache.resolve(command.resourceType());
    validateResourceTypes(commandMapping, patchMapping, patchRawType);
    Map<String, PatchChange> changesByJsonapiName = indexChanges(command.changes(), patchRawType);
    Map<String, MappingProperty> patchProperties = propertiesByJsonapiName(patchMapping);
    Map<String, MappingProperty> commandProperties = propertiesByJsonapiName(commandMapping);
    validateRepresentableChanges(changesByJsonapiName, patchProperties, patchRawType);
    Map<String, @Nullable Object> constructorArguments =
        buildConstructorArguments(
            patchProperties, commandProperties, changesByJsonapiName, patchRawType);
    if (!patchRawType.isRecord()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE,
          patchRawType,
          null,
          "Patch DTO " + patchRawType.getName() + " must be a record type");
    }
    return constructRecord(patchRawType, constructorArguments);
  }

  private static void validateResourceTypes(
      ResourceMapping commandMapping, ResourceMapping patchMapping, Class<?> patchRawType) {
    if (!commandMapping.resourceType().equals(patchMapping.resourceType())) {
      throw new JsonApiMappingException(
          MappingDiagnostic.RESOURCE_TYPE_MISMATCH,
          patchRawType,
          "/type",
          "Patch DTO resource type '"
              + patchMapping.resourceType()
              + "' does not match command resource type '"
              + commandMapping.resourceType()
              + "'");
    }
  }

  private static Map<String, PatchChange> indexChanges(
      List<PatchChange> changes, Class<?> patchRawType) {
    Map<String, PatchChange> indexed = LinkedHashMap.newLinkedHashMap(changes.size());
    for (PatchChange change : changes) {
      PatchChange previous = indexed.putIfAbsent(change.jsonapiName(), change);
      if (previous != null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE,
            patchRawType,
            "/" + change.jsonapiName(),
            "Duplicate supplied change for JSON:API member '" + change.jsonapiName() + "'");
      }
    }
    return indexed;
  }

  private static Map<String, MappingProperty> propertiesByJsonapiName(ResourceMapping mapping) {
    Map<String, MappingProperty> properties =
        LinkedHashMap.newLinkedHashMap(
            mapping.attributes().size() + mapping.relationships().size());
    for (MappingProperty property : mapping.attributes()) {
      properties.put(property.jsonapiName(), property);
    }
    for (MappingProperty property : mapping.relationships()) {
      properties.put(property.jsonapiName(), property);
    }
    return properties;
  }

  private static void validateRepresentableChanges(
      Map<String, PatchChange> changesByJsonapiName,
      Map<String, MappingProperty> patchProperties,
      Class<?> patchRawType) {
    for (Map.Entry<String, PatchChange> entry : changesByJsonapiName.entrySet()) {
      MappingProperty property = patchProperties.get(entry.getKey());
      if (property == null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE,
            patchRawType,
            "/" + entry.getKey(),
            "Supplied change for '"
                + entry.getKey()
                + "' is not representable by patch DTO "
                + patchRawType.getName());
      }
      validateChangeRole(entry.getValue(), property, patchRawType);
    }
  }

  private static void validateChangeRole(
      PatchChange change, MappingProperty property, Class<?> patchRawType) {
    PropertyRole expectedRole =
        change instanceof PatchChange.AttributeChange
            ? PropertyRole.ATTRIBUTE
            : PropertyRole.RELATIONSHIP;
    if (property.role() != expectedRole) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE,
          patchRawType,
          "/" + property.jsonapiName(),
          "Supplied "
              + (expectedRole == PropertyRole.ATTRIBUTE ? "attribute" : "relationship")
              + " change '"
              + property.jsonapiName()
              + "' does not match patch DTO property role");
    }
  }

  private static Map<String, @Nullable Object> buildConstructorArguments(
      Map<String, MappingProperty> patchProperties,
      Map<String, MappingProperty> commandProperties,
      Map<String, PatchChange> changesByJsonapiName,
      Class<?> patchRawType) {
    Map<String, @Nullable Object> arguments =
        LinkedHashMap.newLinkedHashMap(patchProperties.size());
    for (MappingProperty property : patchProperties.values()) {
      JavaType valueType = validatePatchPresenceType(property, patchRawType);
      MappingProperty commandProperty = commandProperties.get(property.jsonapiName());
      if (commandProperty != null) {
        validateValueTypeCompatibility(commandProperty, property, valueType, patchRawType);
      }
      String componentName = property.definition().getInternalName();
      PatchChange change = changesByJsonapiName.get(property.jsonapiName());
      if (change == null) {
        arguments.put(componentName, PatchPresence.omitted());
      } else {
        arguments.put(componentName, PatchPresence.present(change.value()));
      }
    }
    return arguments;
  }

  private static JavaType validatePatchPresenceType(
      MappingProperty property, Class<?> patchRawType) {
    JavaType propertyType = property.accessor().getType();
    if (propertyType.getRawClass() != PatchPresence.class) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE,
          patchRawType,
          "/" + property.logicalName(),
          "Patch DTO property '"
              + property.logicalName()
              + "' must be PatchPresence<T>, not "
              + propertyType.toCanonical());
    }
    if (propertyType.containedTypeCount() < 1) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE,
          patchRawType,
          "/" + property.logicalName(),
          "Patch DTO property '"
              + property.logicalName()
              + "' must declare PatchPresence<T> with a value type parameter");
    }
    return propertyType.containedType(0);
  }

  private static void validateValueTypeCompatibility(
      MappingProperty commandProperty,
      MappingProperty patchProperty,
      JavaType patchValueType,
      Class<?> patchRawType) {
    JavaType sourceType = commandProperty.accessor().getType();
    if (isAssignableValueType(sourceType, patchValueType)) {
      return;
    }
    throw new JsonApiMappingException(
        MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE,
        patchRawType,
        "/" + patchProperty.logicalName(),
        "Patch DTO property '"
            + patchProperty.logicalName()
            + "' PatchPresence value type "
            + patchValueType.toCanonical()
            + " is not compatible with command property type "
            + sourceType.toCanonical());
  }

  private static boolean isAssignableValueType(JavaType source, JavaType target) {
    Class<?> sourceRaw = boxedRaw(source);
    Class<?> targetRaw = boxedRaw(target);
    if (!targetRaw.isAssignableFrom(sourceRaw)) {
      return false;
    }
    if (source.isContainerType() && target.isContainerType()) {
      return isAssignableValueType(source.getContentType(), target.getContentType());
    }
    int contained = target.containedTypeCount();
    if (contained == 0 || source.containedTypeCount() == 0) {
      return true;
    }
    int limit = Math.min(contained, source.containedTypeCount());
    for (int i = 0; i < limit; i++) {
      if (!isAssignableValueType(source.containedType(i), target.containedType(i))) {
        return false;
      }
    }
    return true;
  }

  private static Class<?> boxedRaw(JavaType type) {
    Class<?> raw = type.getRawClass();
    return raw.isPrimitive() ? ClassUtil.wrapperType(raw) : raw;
  }

  private static Object constructRecord(
      Class<?> patchRawType, Map<String, @Nullable Object> constructorArguments) {
    RecordComponent[] components = patchRawType.getRecordComponents();
    Object[] args = new Object[components.length];
    for (int i = 0; i < components.length; i++) {
      RecordComponent component = components[i];
      String name = component.getName();
      if (!constructorArguments.containsKey(name)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.MISSING_CREATOR_INPUT,
            patchRawType,
            "/" + name,
            "Missing patch presence value for record component '" + name + "'");
      }
      args[i] = constructorArguments.get(name);
    }
    try {
      Class<?>[] parameterTypes =
          Arrays.stream(components).map(RecordComponent::getType).toArray(Class<?>[]::new);
      Constructor<?> constructor = patchRawType.getDeclaredConstructor(parameterTypes);
      return constructor.newInstance(args);
    } catch (ReflectiveOperationException | IllegalArgumentException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
          patchRawType,
          "/",
          "Failed to construct record " + patchRawType.getName(),
          e);
    }
  }
}
