package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.bean.BeanDeserializerBase;
import tools.jackson.databind.json.JsonMapper;

/**
 * Whole-meta target-shape rules shared by the entry points that consume whole-meta mappings
 * (ADR-015).
 *
 * <p>The mapping cache/resolver is deliberately kind-agnostic; each consuming entry point validates
 * its own role's wrapper chain against these rules. Read/write and low-level domain mappings allow
 * exactly one optional {@link Optional} wrapper around a Bean / {@link Map} / {@link Object}
 * target; typed PATCH DTOs allow exactly one {@code PatchPresence<T>} wrapper and at most one
 * {@link Optional} inside it.
 *
 * <p>Whether an effective target is a legal whole-meta object target is decided by Jackson, not a
 * manually maintained scalar taxonomy: after rejecting primitives, containers, and the already
 * unwrapped {@link Optional}/{@code PatchPresence} raws, a target is valid iff its root
 * deserializer is a property/creator-driven {@link BeanDeserializerBase} (records, POJOs,
 * constructor-bound beans), or it is {@code Object} or {@link Map}-like. This is the same
 * deserializer signal KAZ-76 uses for its structured-value boundary, so JDK scalars ({@code
 * String}, {@code Character}, {@code Boolean}, {@code Number}, {@code java.time}, {@code
 * java.math}, {@link java.util.UUID}, {@link java.net.URI}/{@link java.net.URL}) and custom scalar
 * deserializers resolve to non-bean deserializers and are rejected. Presence-aware recursion is a
 * separate {@link StructuredValueBinder} decision and is never required here.
 */
final class WholeMetaTarget {

  private final JsonMapper mapper;
  private final Map<JavaType, Boolean> beanShapeCache = new ConcurrentHashMap<>();

  WholeMetaTarget(JsonMapper mapper) {
    this.mapper = mapper;
  }

  /**
   * Read/write and low-level domain-mapping rule: exactly one optional Optional, then
   * Bean/Map/Object.
   */
  boolean validReadWriteTarget(JavaType declared) {
    JavaType effective = unwrapOptional(declared);
    if (isOptional(effective)) {
      return false;
    }
    return isObjectCompatible(effective);
  }

  /**
   * Typed PATCH DTO rule: exactly one PatchPresence, at most one Optional inside, then
   * Bean/Map/Object.
   */
  boolean validTypedPatchTarget(JavaType declared) {
    if (!isPatchPresence(declared)) {
      return false;
    }
    JavaType inner = declared.containedType(0);
    JavaType effective = isOptional(inner) ? inner.containedType(0) : inner;
    if (isOptional(effective)) {
      return false;
    }
    return isObjectCompatible(effective);
  }

  private boolean isObjectCompatible(JavaType type) {
    if (type.isPrimitive() || type.isArrayType() || type.isCollectionLikeType()) {
      return false;
    }
    if (type.isMapLikeType()) {
      return true;
    }
    Class<?> raw = type.getRawClass();
    if (raw == Object.class) {
      return true;
    }
    if (raw == Optional.class
        || raw == PatchPresence.class
        || raw == PatchPresence.Present.class
        || raw == PatchPresence.Omitted.class) {
      return false;
    }
    return isPropertyDrivenBean(type);
  }

  /**
   * True when Jackson resolves a property/creator-driven bean deserializer ({@link
   * BeanDeserializerBase}) for the type — the same structured-value boundary KAZ-76 uses, so a
   * target is object-shaped only when Jackson itself treats it as a bean.
   */
  private boolean isPropertyDrivenBean(JavaType type) {
    return beanShapeCache.computeIfAbsent(
        type,
        ignored -> {
          DeserializationContext context = mapper._deserializationContext();
          ValueDeserializer<?> deserializer = context.findRootValueDeserializer(type);
          return deserializer instanceof BeanDeserializerBase;
        });
  }

  private static boolean isPatchPresence(JavaType type) {
    return type.getRawClass() == PatchPresence.class && type.containedTypeCount() == 1;
  }

  private static boolean isOptional(JavaType type) {
    return type.getRawClass() == Optional.class && type.containedTypeCount() == 1;
  }

  private static JavaType unwrapOptional(JavaType type) {
    return isOptional(type) ? type.containedType(0) : type;
  }
}
