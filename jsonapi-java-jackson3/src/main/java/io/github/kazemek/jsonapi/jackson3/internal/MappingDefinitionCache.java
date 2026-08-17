package io.github.kazemek.jsonapi.jackson3.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.introspect.AnnotatedClass;
import tools.jackson.databind.introspect.ClassIntrospector;
import tools.jackson.databind.json.JsonMapper;

public final class MappingDefinitionCache {

  private final JsonMapper mapper;
  private final Map<CacheKey, ResourceMapping> cache = new ConcurrentHashMap<>();

  public MappingDefinitionCache(JsonMapper mapper) {
    this.mapper = mapper;
  }

  ResourceMapping resolve(Class<?> rawType) {
    return resolve(mapper.constructType(rawType));
  }

  /**
   * Resolves the mapping for {@code javaType}, preserving full parameterization so property types
   * introspect with type variables bound (for example {@code GenericPatch<String>} resolves its
   * members as {@code String}). The cache key already holds the {@link JavaType}, so distinct
   * parameterizations of the same raw class map independently.
   */
  ResourceMapping resolve(JavaType javaType) {
    SerializationConfig config = mapper.serializationConfig();
    int configHash = configHash(config);
    CacheKey key = new CacheKey(javaType, configHash);
    ResourceMapping existing = cache.get(key);
    if (existing != null) {
      return existing;
    }
    Class<?> rawType = javaType.getRawClass();
    return cache.computeIfAbsent(key, cacheKey -> computeMapping(rawType, javaType, config));
  }

  private ResourceMapping computeMapping(
      Class<?> rawType, JavaType javaType, SerializationConfig config) {
    ClassIntrospector introspector = config.classIntrospectorInstance();
    AnnotatedClass annotatedClass = introspector.introspectClassAnnotations(javaType);
    BeanDescription beanDescription =
        introspector.introspectForSerialization(javaType, annotatedClass);
    return MappingDefinitionResolver.resolve(beanDescription, rawType);
  }

  private static int configHash(SerializationConfig config) {
    int result =
        config.getPropertyNamingStrategy() != null
            ? config.getPropertyNamingStrategy().hashCode()
            : 0;
    result = 31 * result + config.getDefaultVisibilityChecker().hashCode();
    return result;
  }

  private record CacheKey(JavaType type, int configHash) {}
}
