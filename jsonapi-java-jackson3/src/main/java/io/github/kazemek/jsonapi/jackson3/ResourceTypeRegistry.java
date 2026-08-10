package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.validation.MemberNames;
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;

/**
 * Explicit JSON:API resource type to DTO target registry for {@link JsonApiDomainDocumentReader}.
 *
 * <p>Build via {@link #builder()} and {@link Builder#register(Class)} / {@link
 * Builder#register(JavaType)}. Registration records only the target {@link Class} or {@link
 * JavaType} and keys it by reading {@link JsonApiResource#type()} from the registered raw class —
 * plain annotation lookup, no introspection and no {@link tools.jackson.databind.json.JsonMapper}
 * is retained. Missing {@link JsonApiResource} or empty/invalid type names fail at {@link
 * Builder#register(Class)} with {@link MappingDiagnostic#MISSING_RESOURCE_ANNOTATION} / {@link
 * MappingDiagnostic#INVALID_RESOURCE_TYPE}; duplicate JSON:API type names fail at {@link
 * Builder#build()} with {@link MappingDiagnostic#CONFLICTING_TYPE_REGISTRATION}.
 *
 * <p>An empty registry is legal: identifier, error, and meta-only documents bind without resource
 * DTOs, while any resource document whose primary or included type is unregistered fails at bind
 * time with {@link MappingDiagnostic#UNREGISTERED_RESOURCE_TYPE}.
 */
public final class ResourceTypeRegistry {

  private final Map<String, RegisteredType> registrations;

  private ResourceTypeRegistry(Map<String, RegisteredType> registrations) {
    this.registrations = Map.copyOf(registrations);
  }

  /** Returns a builder for a new registry. */
  public static Builder builder() {
    return new Builder();
  }

  /** Resolves the registered target for the given JSON:API type name, or {@code null}. */
  @Nullable RegisteredType resolve(String jsonApiType) {
    return registrations.get(jsonApiType);
  }

  /** Fluent builder for {@link ResourceTypeRegistry}. */
  public static final class Builder {

    private final List<RegisteredType> registrations = new ArrayList<>();

    private Builder() {}

    /** Registers the given DTO class under its {@link JsonApiResource#type()}. */
    public Builder register(Class<?> targetClass) {
      Objects.requireNonNull(targetClass, "targetClass");
      registrations.add(new RegisteredType(typeKey(targetClass), targetClass, null));
      return this;
    }

    /** Registers the given DTO Java type under its raw class's {@link JsonApiResource#type()}. */
    public Builder register(JavaType targetType) {
      Objects.requireNonNull(targetType, "targetType");
      Class<?> rawClass = targetType.getRawClass();
      registrations.add(new RegisteredType(typeKey(rawClass), rawClass, targetType));
      return this;
    }

    /** Builds the registry, failing on duplicate JSON:API type names. */
    public ResourceTypeRegistry build() {
      Map<String, RegisteredType> resolved = new LinkedHashMap<>();
      for (RegisteredType registration : registrations) {
        RegisteredType existing = resolved.putIfAbsent(registration.type(), registration);
        if (existing != null) {
          throw new JsonApiMappingException(
              MappingDiagnostic.CONFLICTING_TYPE_REGISTRATION,
              registration.rawClass(),
              registration.type(),
              "Conflicting JSON:API type '"
                  + registration.type()
                  + "' registered by "
                  + existing.rawClass().getName()
                  + " and "
                  + registration.rawClass().getName());
        }
      }
      return new ResourceTypeRegistry(resolved);
    }

    private static String typeKey(Class<?> rawClass) {
      JsonApiResource resourceAnnotation = rawClass.getAnnotation(JsonApiResource.class);
      if (resourceAnnotation == null) {
        throw new JsonApiMappingException(
            MappingDiagnostic.MISSING_RESOURCE_ANNOTATION,
            rawClass,
            null,
            "Missing @JsonApiResource on " + rawClass.getName());
      }
      String resourceType = resourceAnnotation.type();
      if (resourceType.isEmpty()) {
        throw new JsonApiMappingException(
            MappingDiagnostic.INVALID_RESOURCE_TYPE,
            rawClass,
            null,
            "@JsonApiResource.type() must not be empty on " + rawClass.getName());
      }
      if (!MemberNames.isValid(resourceType)) {
        throw new JsonApiMappingException(
            MappingDiagnostic.INVALID_RESOURCE_TYPE,
            rawClass,
            null,
            "Invalid resource type name: " + resourceType);
      }
      return resourceType;
    }
  }

  /**
   * One recorded registration: the type key and raw class, plus the registered {@link JavaType}
   * when the caller registered a generic target.
   */
  record RegisteredType(String type, Class<?> rawClass, @Nullable JavaType javaType) {}
}
