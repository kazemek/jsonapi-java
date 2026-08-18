package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;

/**
 * Presence-aware nested PATCH shape whose canonical creator throws when constructed from a supplied
 * value, forcing a Jackson construction failure whose deep path must be translated to a wire-name
 * pointer (ADR-014).
 */
public record ThrowingGeoPatch(PatchPresence<String> lat) {

  public ThrowingGeoPatch {
    if (lat instanceof PatchPresence.Present) {
      throw new IllegalStateException("boom");
    }
  }
}
