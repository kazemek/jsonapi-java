package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JavaType;

/**
 * Whole-meta target-shape rules shared by the entry points that consume whole-meta mappings
 * (ADR-015).
 *
 * <p>The mapping cache/resolver is deliberately kind-agnostic; each consuming entry point validates
 * its own role's wrapper chain against these rules. Read/write and low-level domain mappings allow
 * exactly one optional {@link Optional} wrapper around a Bean / {@link Map} / {@link Object}
 * target; typed PATCH DTOs allow exactly one {@code PatchPresence<T>} wrapper and at most one
 * {@link Optional} inside it. Containers, scalars, primitives, enums, raw {@code PatchPresence},
 * and nested wrapper combinations are invalid.
 */
final class WholeMetaTarget {

  private WholeMetaTarget() {}

  /**
   * Read/write and low-level domain-mapping rule: exactly one optional Optional, then
   * Bean/Map/Object.
   */
  static boolean validReadWriteTarget(JavaType declared) {
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
  static boolean validTypedPatchTarget(JavaType declared) {
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

  static boolean isPatchPresence(JavaType type) {
    return type.getRawClass() == PatchPresence.class && type.containedTypeCount() == 1;
  }

  private static boolean isOptional(JavaType type) {
    return type.getRawClass() == Optional.class && type.containedTypeCount() == 1;
  }

  private static JavaType unwrapOptional(JavaType type) {
    return isOptional(type) ? type.containedType(0) : type;
  }

  private static boolean isObjectCompatible(JavaType type) {
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
    if (raw.isEnum()
        || raw == String.class
        || raw == Character.class
        || raw == Boolean.class
        || Number.class.isAssignableFrom(raw)) {
      return false;
    }
    return true;
  }
}
