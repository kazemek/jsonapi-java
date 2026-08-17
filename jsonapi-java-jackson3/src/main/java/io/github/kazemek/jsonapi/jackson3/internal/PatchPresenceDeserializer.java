package io.github.kazemek.jsonapi.jackson3.internal;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

/**
 * Minimal contextual deserializer that reconstructs {@link PatchPresence} from an internal {@link
 * PresenceMarker} object.
 *
 * <p>{@link #createContextual} captures the property's single {@code PatchPresence} type argument
 * as the inner {@link JavaType}. {@code present=false} yields {@link PatchPresence#omitted()};
 * {@code present=true} yields {@link PatchPresence#present(Object)} with the {@code value} member
 * read through the inner type. When the {@code value} member is absent (dropped by a caller
 * inclusion configuration such as {@code NON_EMPTY}) or is JSON {@code null}, the inner type's null
 * value is used (for example {@code Optional.empty()} for an {@link java.util.Optional} inner), so
 * the tri-state never collapses under caller serialization configuration.
 */
final class PatchPresenceDeserializer extends ValueDeserializer<PatchPresence<?>> {

  private static final String PRESENT = "present";
  private static final String VALUE = "value";

  private final @Nullable JavaType inner;

  PatchPresenceDeserializer() {
    this.inner = null;
  }

  private PatchPresenceDeserializer(JavaType inner) {
    this.inner = inner;
  }

  @Override
  public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
    JavaType contextualType = ctxt.getContextualType();
    JavaType resolvedInner =
        contextualType != null && contextualType.containedTypeCount() == 1
            ? contextualType.containedType(0)
            : ctxt.constructType(Object.class);
    return new PatchPresenceDeserializer(resolvedInner);
  }

  @Override
  public PatchPresence<?> deserialize(JsonParser parser, DeserializationContext ctxt) {
    JavaType innerType = inner != null ? inner : ctxt.constructType(Object.class);
    if (!parser.isExpectedStartObjectToken()) {
      return PatchPresence.present(ctxt.readValue(parser, innerType));
    }
    boolean present = false;
    boolean sawValue = false;
    @Nullable Object rawValue = null;
    JsonToken token = parser.nextToken();
    while (token != JsonToken.END_OBJECT) {
      String name = parser.currentName();
      parser.nextToken();
      if (PRESENT.equals(name)) {
        present = parser.getBooleanValue();
      } else if (VALUE.equals(name)) {
        sawValue = true;
        if (parser.currentToken() == JsonToken.VALUE_NULL) {
          rawValue = nullValue(ctxt, innerType);
        } else {
          rawValue = ctxt.readValue(parser, innerType);
        }
      } else {
        parser.skipChildren();
      }
      token = parser.nextToken();
    }
    if (!present) {
      return PatchPresence.omitted();
    }
    if (!sawValue) {
      rawValue = nullValue(ctxt, innerType);
    }
    return PatchPresence.present(rawValue);
  }

  private static @Nullable Object nullValue(DeserializationContext ctxt, JavaType innerType) {
    return ctxt.findRootValueDeserializer(innerType).getNullValue(ctxt);
  }
}
