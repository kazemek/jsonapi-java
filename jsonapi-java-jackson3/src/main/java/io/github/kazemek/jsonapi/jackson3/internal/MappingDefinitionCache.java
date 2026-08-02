package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson3.JsonApiMappingException;
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
    SerializationConfig config = mapper.serializationConfig();
    JavaType javaType = mapper.constructType(rawType);
    int configHash = configHash(config);
    CacheKey key = new CacheKey(javaType, configHash);
    ResourceMapping existing = cache.get(key);
    if (existing != null) {
      return existing;
    }
    return cache.computeIfAbsent(key, cacheKey -> computeMapping(rawType, javaType, config));
  }

  private ResourceMapping computeMapping(
      Class<?> rawType, JavaType javaType, SerializationConfig config) {
    try {
      ClassIntrospector introspector = config.classIntrospectorInstance();
      AnnotatedClass annotatedClass = introspector.introspectClassAnnotations(javaType);
      BeanDescription beanDescription =
          introspector.introspectForSerialization(javaType, annotatedClass);
      return MappingDefinitionResolver.resolve(beanDescription, rawType);
    } catch (JsonApiMappingException e) {
      throw e;
    }
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
