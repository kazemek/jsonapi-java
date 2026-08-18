package io.github.kazemek.jsonapi.jackson;

import java.util.Objects;
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
      value = PatchValues.freeze(value);
    }

    @Override
    public @Nullable Object value() {
      return PatchValues.expose(value);
    }
  }

  /** A supplied mapped relationship linkage replacement. */
  record RelationshipChange(String jsonapiName, String logicalName, @Nullable Object value)
      implements PatchChange {
    public RelationshipChange {
      Objects.requireNonNull(jsonapiName, "jsonapiName");
      Objects.requireNonNull(logicalName, "logicalName");
      value = PatchValues.freeze(value);
    }

    @Override
    public @Nullable Object value() {
      return PatchValues.expose(value);
    }
  }
}
