package io.github.kazemek.jsonapi.jackson;

import org.jspecify.annotations.Nullable;

/**
 * Presence state for one patchable member on an application-owned patch DTO.
 *
 * <p>Used with {@linkplain PatchCommand typed PATCH projection}: {@link Omitted} means the update
 * did not supply the member; {@link Present} means it was supplied, with {@code value == null}
 * representing explicit JSON {@code null} or null relationship linkage.
 *
 * @param <T> the converted attribute or relationship value type
 */
@SuppressWarnings("java:S2326")
public sealed interface PatchPresence<T> permits PatchPresence.Omitted, PatchPresence.Present {

  /** No requested change for this member. */
  record Omitted<T>() implements PatchPresence<T> {
    /** Marker variant; {@code T} documents the wrapped value type for callers. */
    public Omitted {}
  }

  /** Supplied change; {@code null} value means explicit null / null linkage, not omission. */
  record Present<T>(@Nullable T value) implements PatchPresence<T> {}

  /** Returns {@link Omitted} for the given type parameter. */
  static <T> PatchPresence<T> omitted() {
    return new Omitted<>();
  }

  /** Returns {@link Present} wrapping the supplied converted value. */
  static <T> PatchPresence<T> present(@Nullable T value) {
    return new Present<>(value);
  }
}
