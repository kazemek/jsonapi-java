package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Objects;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Presence-aware nested PATCH shape (JavaBean-style) with wrapper-level {@code @JsonDeserialize} on
 * the {@code city} setter, proving deserialization-side customization on a setter is detected and
 * rejected on typed shape entry (ADR-014).
 */
public final class SetterCustomizedAddressPatch {

  private PatchPresence<String> street;
  private PatchPresence<String> city;

  public SetterCustomizedAddressPatch() {}

  public SetterCustomizedAddressPatch(PatchPresence<String> street, PatchPresence<String> city) {
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

  @JsonDeserialize(using = UpperCaseStringDeserializer.class)
  public void setCity(PatchPresence<String> city) {
    this.city = city;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof SetterCustomizedAddressPatch that)) {
      return false;
    }
    return Objects.equals(street, that.street) && Objects.equals(city, that.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, city);
  }
}
