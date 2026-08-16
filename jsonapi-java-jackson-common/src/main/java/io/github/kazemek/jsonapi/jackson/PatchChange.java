package io.github.kazemek.jsonapi.jackson;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * One requested change in a {@link PatchCommand}: a supplied mapped attribute or relationship.
 *
 * <p>Omitted members never appear. Explicit attribute JSON {@code null} is {@code value == null} on
 * a present {@link AttributeChange}. Relationship {@code NullLinkage} is Java {@code null} or empty
 * {@code Optional} as the Jackson adapter binder would produce.
 */
public sealed interface PatchChange
    permits PatchChange.AttributeChange, PatchChange.RelationshipChange {

  /** Final JSON:API member name (including attribute/relationship renames). */
  String jsonapiName();

  /** Jackson logical property name on the annotated DTO. */
  String logicalName();

  /** Converted property value; {@code null} means explicit null / null linkage, not omission. */
  @Nullable Object value();

  /** A supplied mapped attribute change. */
  record AttributeChange(String jsonapiName, String logicalName, @Nullable Object value)
      implements PatchChange {
    public AttributeChange {
      Objects.requireNonNull(jsonapiName, "jsonapiName");
      Objects.requireNonNull(logicalName, "logicalName");
      value = freezeValue(value);
    }
  }

  /** A supplied mapped relationship linkage replacement. */
  record RelationshipChange(String jsonapiName, String logicalName, @Nullable Object value)
      implements PatchChange {
    public RelationshipChange {
      Objects.requireNonNull(jsonapiName, "jsonapiName");
      Objects.requireNonNull(logicalName, "logicalName");
      value = freezeValue(value);
    }
  }

  /**
   * Shallow-freeze {@link List}, {@link Set}, and array values. Null elements are preserved; other
   * objects are left as-is.
   */
  static @Nullable Object freezeValue(@Nullable Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof List<?> list) {
      List<@Nullable Object> copy = new ArrayList<>(list.size());
      copy.addAll(list);
      return Collections.unmodifiableList(copy);
    }
    if (value instanceof Set<?> set) {
      Set<@Nullable Object> copy = new LinkedHashSet<>(set);
      return Collections.unmodifiableSet(copy);
    }
    Class<?> type = value.getClass();
    if (type.isArray()) {
      int length = Array.getLength(value);
      Object copy = Array.newInstance(type.getComponentType(), length);
      //noinspection SuspiciousSystemArraycopy
      System.arraycopy(value, 0, copy, 0, length);
      return copy;
    }
    return value;
  }
}
