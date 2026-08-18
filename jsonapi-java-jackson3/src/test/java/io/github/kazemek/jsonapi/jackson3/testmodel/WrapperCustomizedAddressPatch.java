package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Objects;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Presence-aware nested PATCH shape with a wrapper-level {@code @JsonDeserialize}-customized
 * member, proving nested wrapper customization is rejected on shape entry (ADR-014).
 */
public final class WrapperCustomizedAddressPatch {

  private PatchPresence<String> street;
  private PatchPresence<String> city;

  public WrapperCustomizedAddressPatch() {}

  public WrapperCustomizedAddressPatch(PatchPresence<String> street, PatchPresence<String> city) {
    this.street = street;
    this.city = city;
  }

  public PatchPresence<String> getStreet() {
    return street;
  }

  public void setStreet(PatchPresence<String> street) {
    this.street = street;
  }

  @JsonDeserialize(using = UpperCaseStringDeserializer.class)
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
    if (!(other instanceof WrapperCustomizedAddressPatch that)) {
      return false;
    }
    return Objects.equals(street, that.street) && Objects.equals(city, that.city);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, city);
  }
}
