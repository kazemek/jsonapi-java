package io.github.kazemek.jsonapi.jackson3.testmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Objects;

/**
 * Presence-aware nested whole-meta PATCH shape whose setter throws when a supplied member is
 * present, forcing a genuine Jackson construction failure during the final DTO construction whose
 * deep path must be translated to a wire-name pointer (ADR-015). The {@code source} member's
 * logical name differs from its wire name {@code w_source}.
 */
public final class ThrowingMetaPatch {

  private PatchPresence<String> source;
  private PatchPresence<String> note;

  public ThrowingMetaPatch() {}

  public ThrowingMetaPatch(PatchPresence<String> source, PatchPresence<String> note) {
    this.source = source;
    this.note = note;
  }

  @JsonProperty("w_source")
  public PatchPresence<String> getSource() {
    return source;
  }

  public void setSource(PatchPresence<String> source) {
    if (source instanceof PatchPresence.Present) {
      throw new IllegalStateException("boom");
    }
    this.source = source;
  }

  public PatchPresence<String> getNote() {
    return note;
  }

  public void setNote(PatchPresence<String> note) {
    this.note = note;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ThrowingMetaPatch that)) {
      return false;
    }
    return Objects.equals(source, that.source) && Objects.equals(note, that.note);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, note);
  }
}
