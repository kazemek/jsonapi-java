package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson.PatchChange;
import io.github.kazemek.jsonapi.jackson.PatchCommand;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

/**
 * Projects a {@link PatchCommand} into an application-owned patch DTO without re-reading JSON or
 * mutating domain state.
 *
 * <p>Each patchable property on the target type must be {@link PatchPresence}; supplied changes
 * outside the target surface fail explicitly.
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
    ResourceMapping patchMapping = cache.resolvePatch(patchRawType);
    ResourceMapping commandMapping = cache.resolve(command.resourceType());
    validateResourceTypes(commandMapping, patchMapping, patchRawType);
    Map<String, PatchChange> changesByLogicalName = indexChanges(command.changes(), patchRawType);
    Map<String, MappingProperty> patchProperties = patchPropertiesByLogicalName(patchMapping);
    validateRepresentableChanges(changesByLogicalName, patchProperties, patchRawType);
    Map<String, @Nullable Object> properties =
        buildPresenceProperties(patchProperties, changesByLogicalName, patchRawType);
    if (!patchRawType.isRecord()) {
      throw new JsonApiMappingException(
          MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE,
          patchRawType,
          null,
          "Patch DTO " + patchRawType.getName() + " must be a record type");
    }
    return constructRecord(patchRawType, properties);
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
      PatchChange previous = indexed.putIfAbsent(change.logicalName(), change);
      if (previous != null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.UNREPRESENTABLE_PATCH_CHANGE,
            patchRawType,
            "/" + change.logicalName(),
            "Duplicate supplied change for property '" + change.logicalName() + "'");
      }
    }
    return indexed;
  }

  private static Map<String, MappingProperty> patchPropertiesByLogicalName(
      ResourceMapping patchMapping) {
    Map<String, MappingProperty> properties =
        LinkedHashMap.newLinkedHashMap(
            patchMapping.attributes().size() + patchMapping.relationships().size());
    for (MappingProperty property : patchMapping.attributes()) {
      properties.put(property.logicalName(), property);
    }
    for (MappingProperty property : patchMapping.relationships()) {
      properties.put(property.logicalName(), property);
    }
    return properties;
  }

  private static void validateRepresentableChanges(
      Map<String, PatchChange> changesByLogicalName,
      Map<String, MappingProperty> patchProperties,
      Class<?> patchRawType) {
    for (Map.Entry<String, PatchChange> entry : changesByLogicalName.entrySet()) {
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
          "/" + property.logicalName(),
          "Supplied "
              + (expectedRole == PropertyRole.ATTRIBUTE ? "attribute" : "relationship")
              + " change '"
              + property.logicalName()
              + "' does not match patch DTO property role");
    }
  }

  private Map<String, @Nullable Object> buildPresenceProperties(
      Map<String, MappingProperty> patchProperties,
      Map<String, PatchChange> changesByLogicalName,
      Class<?> patchRawType) {
    Map<String, @Nullable Object> properties =
        LinkedHashMap.newLinkedHashMap(patchProperties.size());
    for (MappingProperty property : patchProperties.values()) {
      validatePatchPresenceType(property, patchRawType);
      PatchChange change = changesByLogicalName.get(property.logicalName());
      if (change == null) {
        properties.put(property.logicalName(), PatchPresence.omitted());
      } else {
        properties.put(property.logicalName(), PatchPresence.present(change.value()));
      }
    }
    return properties;
  }

  private static void validatePatchPresenceType(MappingProperty property, Class<?> patchRawType) {
    JavaType propertyType = property.accessor().getType();
    if (!propertyType.isTypeOrSubTypeOf(PatchPresence.class)) {
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
  }

  private static Object constructRecord(
      Class<?> patchRawType, Map<String, @Nullable Object> properties) {
    RecordComponent[] components = patchRawType.getRecordComponents();
    Object[] args = new Object[components.length];
    for (int i = 0; i < components.length; i++) {
      RecordComponent component = components[i];
      String name = component.getName();
      if (!properties.containsKey(name)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.MISSING_CREATOR_INPUT,
            patchRawType,
            "/" + name,
            "Missing patch presence value for record component '" + name + "'");
      }
      args[i] = properties.get(name);
    }
    try {
      Class<?>[] parameterTypes =
          java.util.Arrays.stream(components)
              .map(RecordComponent::getType)
              .toArray(Class<?>[]::new);
      Constructor<?> constructor = patchRawType.getDeclaredConstructor(parameterTypes);
      return constructor.newInstance(args);
    } catch (ReflectiveOperationException e) {
      throw new JsonApiMappingException(
          MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE,
          patchRawType,
          "/",
          "Failed to construct record " + patchRawType.getName(),
          e);
    }
  }
}
