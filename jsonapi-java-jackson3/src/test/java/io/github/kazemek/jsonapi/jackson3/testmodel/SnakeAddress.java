package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;

/**
 * Ordinary non-record structured domain value type with a multi-word member, proving the naming
 * strategy applies to low-level structured traversal and that {@code wireName} / {@code
 * logicalName} divergence is preserved in the {@link
 * io.github.kazemek.jsonapi.jackson.StructuredPatch} (ADR-014).
 */
public final class SnakeAddress {

  private String streetName;

  public SnakeAddress() {}

  public SnakeAddress(String streetName) {
    this.streetName = streetName;
  }

  public String getStreetName() {
    return streetName;
  }

  public void setStreetName(String streetName) {
    this.streetName = streetName;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SnakeAddress that)) {
      return false;
    }
    return Objects.equals(streetName, that.streetName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(streetName);
  }
}
