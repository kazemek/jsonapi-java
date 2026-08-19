package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Objects;

/**
 * JavaBean-style presence-aware nested PATCH shape with a multi-word member, used to prove the
 * naming strategy applies to nested structured marker maps (ADR-014).
 */
public final class SnakeAddressPatch {

  private PatchPresence<String> streetName;
  private PatchPresence<String> city;

  public SnakeAddressPatch() {}

  public SnakeAddressPatch(PatchPresence<String> streetName, PatchPresence<String> city) {
    this.streetName = streetName;
    this.city = city;
  }

  public PatchPresence<String> getStreetName() {
    return streetName;
  }

  public void setStreetName(PatchPresence<String> streetName) {
    this.streetName = streetName;
  }

  public PatchPresence<String> getCity() {
    return city;
  }

  public void setCity(PatchPresence<String> city) {
    this.city = city;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SnakeAddressPatch that)) {
      return false;
    }
    return Objects.equals(streetName, that.streetName) && Objects.equals(city, that.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(streetName, city);
  }
}
