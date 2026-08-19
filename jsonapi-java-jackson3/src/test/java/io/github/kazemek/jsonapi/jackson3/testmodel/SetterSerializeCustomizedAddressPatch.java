package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Objects;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Presence-aware nested PATCH shape (JavaBean-style) with wrapper-level {@code @JsonSerialize} on
 * the {@code city} setter, proving serialization customization on a non-getter side is detected
 * symmetrically and rejected on typed shape entry (ADR-014).
 */
public final class SetterSerializeCustomizedAddressPatch {

  private PatchPresence<String> street;
  private PatchPresence<String> city;

  public SetterSerializeCustomizedAddressPatch() {}

  public SetterSerializeCustomizedAddressPatch(
      PatchPresence<String> street, PatchPresence<String> city) {
    this.street = street;
    this.city = city;
  }

  public PatchPresence<String> getStreet() {
    return street;
  }

  public void setStreet(PatchPresence<String> street) {
    this.street = street;
  }

  public PatchPresence<String> getCity() {
    return city;
  }

  @JsonSerialize(using = ConstantPatchPresenceSerializer.class)
  public void setCity(PatchPresence<String> city) {
    this.city = city;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SetterSerializeCustomizedAddressPatch that)) {
      return false;
    }
    return Objects.equals(street, that.street) && Objects.equals(city, that.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, city);
  }
}
