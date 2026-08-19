package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;

/**
 * Ordinary traversable structured domain value supertype used by the setter-level
 * {@code @JsonDeserialize(as = ...)} low-level fixtures (ADR-014). Without the {@code as}
 * refinement this concrete bean would recurse on the low-level path.
 */
public class BaseProfile {

  private String name;

  public BaseProfile() {}

  public BaseProfile(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof BaseProfile that)) {
      return false;
    }
    return Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }
}
