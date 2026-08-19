package io.github.kazemek.jsonapi.jackson3.testmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.Objects;

/**
 * Abstract polymorphic whole-meta base type: Jackson materializes the concrete subtype from the
 * {@code kind} discriminator through the property/root {@code TypeDeserializer} (ADR-015).
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
@JsonSubTypes({@JsonSubTypes.Type(value = SourceMeta.class, name = "source")})
public abstract class PolyMetaBase {

  private String note;

  public PolyMetaBase() {}

  public PolyMetaBase(String note) {
    this.note = note;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof PolyMetaBase that)) {
      return false;
    }
    return Objects.equals(note, that.note);
  }

  @Override
  public int hashCode() {
    return Objects.hash(note);
  }
}
