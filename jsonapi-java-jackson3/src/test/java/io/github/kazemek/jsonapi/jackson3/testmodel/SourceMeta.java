package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;

/** Concrete {@link PolyMetaBase} subtype selected from the {@code kind} discriminator. */
public final class SourceMeta extends PolyMetaBase {

  private String source;

  public SourceMeta() {}

  public SourceMeta(String source, String note) {
    super(note);
    this.source = source;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SourceMeta that)) {
      return false;
    }
    return Objects.equals(source, that.source) && super.equals(that);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, super.hashCode());
  }
}
