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
    return resolve(mapper.constructType(rawType), false);
  }

  ResourceMapping resolvePatch(JavaType javaType) {
    return resolve(javaType, true);
  }

  private ResourceMapping resolve(JavaType javaType, boolean patchDto) {
    SerializationConfig config = mapper.serializationConfig();
    int configHash = configHash(config);
    CacheKey key = new CacheKey(javaType, configHash, patchDto);
    ResourceMapping existing = cache.get(key);
    if (existing != null) {
      return existing;
    }
    return cache.computeIfAbsent(key, cacheKey -> computeMapping(javaType, config, patchDto));
  }

  private ResourceMapping computeMapping(
      JavaType javaType, SerializationConfig config, boolean patchDto) {
    Class<?> rawType = javaType.getRawClass();
    ClassIntrospector introspector = config.classIntrospectorInstance();
    AnnotatedClass annotatedClass = introspector.introspectClassAnnotations(javaType);
    BeanDescription beanDescription =
        introspector.introspectForSerialization(javaType, annotatedClass);
    return patchDto
        ? MappingDefinitionResolver.resolvePatchDto(beanDescription, rawType)
        : MappingDefinitionResolver.resolve(beanDescription, rawType);
  }

  private static int configHash(SerializationConfig config) {
    int result =
        config.getPropertyNamingStrategy() != null
            ? config.getPropertyNamingStrategy().hashCode()
            : 0;
    result = 31 * result + config.getDefaultVisibilityChecker().hashCode();
    return result;
  }

  private record CacheKey(JavaType type, int configHash, boolean patchDto) {}
}
