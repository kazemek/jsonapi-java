package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException;
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic;
import io.github.kazemek.jsonapi.jackson3.internal.MappingDefinitionCache;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Explicit JSON:API resource type to DTO target registry for {@link JsonApiDomainDocumentReader}.
 *
 * <p>Build via {@link #builder(JsonMapper)} and {@link Builder#register(Class)} / {@link
 * Builder#register(JavaType)}. The no-argument {@link #builder()} convenience uses a default mapper
 * for applications that have no mapper-specific configuration; callers with configured Jackson
 * should pass that mapper explicitly. The registry is a dispatch mechanism only: it answers "wire
 * JSON:API resource type &rarr; Java target type" and never interprets annotations itself.
 * Registration keys each target by the class-level resource metadata of the <em>configured</em>
 * {@link JsonMapper} passed to {@link #builder(JsonMapper)} — the same canonical configured-Jackson
 * authority that drives mapping — so class-level mix-ins are honored exactly as in domain write,
 * read, and PATCH paths. The built registry retains no mapper. Missing configured resource metadata
 * or empty/invalid type names fail at {@link Builder#register(Class)} with {@link
 * MappingDiagnostic#MISSING_RESOURCE_ANNOTATION} / {@link MappingDiagnostic#INVALID_RESOURCE_TYPE};
 * duplicate JSON:API type names fail at {@link Builder#build()} with {@link
 * MappingDiagnostic#CONFLICTING_TYPE_REGISTRATION}.
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

  /**
   * Returns a builder keyed by the class-level resource metadata of the given configured mapper.
   * Pass the same configured mapper you hand to {@link JsonApiJackson3#domainDocumentReader} so
   * registration keys and binding agree on configured metadata such as mix-ins.
   */
  public static Builder builder(JsonMapper mapper) {
    return new Builder(mapper);
  }

  /**
   * Returns a builder using a default mapper. Use {@link #builder(JsonMapper)} when the registry
   * must honor caller configuration such as class-level mix-ins.
   */
  public static Builder builder() {
    return builder(JsonMapper.builder().build());
  }

  /** Resolves the registered target for the given JSON:API type name, or {@code null}. */
  @Nullable RegisteredType resolve(String jsonApiType) {
    return registrations.get(jsonApiType);
  }

  /** Fluent builder for {@link ResourceTypeRegistry}. */
  public static final class Builder {

    private final MappingDefinitionCache metadataAuthority;
    private final List<RegisteredType> registrations = new ArrayList<>();

    private Builder(JsonMapper mapper) {
      Objects.requireNonNull(mapper, "mapper");
      this.metadataAuthority = new MappingDefinitionCache(mapper);
    }

    /**
     * Registers the given DTO class under its configured class-level JSON:API resource type.
     *
     * @throws JsonApiMappingException {@link MappingDiagnostic#MISSING_RESOURCE_ANNOTATION} or
     *     {@link MappingDiagnostic#INVALID_RESOURCE_TYPE} when the configured mapper sees no valid
     *     resource metadata for the raw class
     */
    public Builder register(Class<?> targetClass) {
      Objects.requireNonNull(targetClass, "targetClass");
      registrations.add(new RegisteredType(typeKey(targetClass), targetClass, null));
      return this;
    }

    /**
     * Registers the given DTO Java type under its raw class's configured class-level JSON:API
     * resource type.
     *
     * @throws JsonApiMappingException {@link MappingDiagnostic#MISSING_RESOURCE_ANNOTATION} or
     *     {@link MappingDiagnostic#INVALID_RESOURCE_TYPE} when the configured mapper sees no valid
     *     resource metadata for the raw class
     */
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
          // Registry conflicts have no document or member location; the conflicting type name and
          // classes stay in the message per the mapping-location contract.
          throw JsonApiMappingException.withoutLocation(
              MappingDiagnostic.CONFLICTING_TYPE_REGISTRATION,
              registration.rawClass(),
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

    private String typeKey(Class<?> rawClass) {
      return metadataAuthority.requireResourceTypeName(rawClass);
    }
  }

  /**
   * One recorded registration: the type key and raw class, plus the registered {@link JavaType}
   * when the caller registered a generic target.
   */
  record RegisteredType(String type, Class<?> rawClass, @Nullable JavaType javaType) {}
}
