package io.github.kazemek.jsonapi.jackson3.testmodel;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;

/**
 * Ordinary structured domain value type whose {@code city} setter carries {@code @JsonSetter(nulls
 * = Nulls.AS_EMPTY)}, proving nested explicit null converts through the containing property's null
 * provider ({@code ""}) rather than the root target deserializer's null value ({@code null}) on the
 * low-level path (ADR-014).
 */
public final class OuterWithNullEmptyCity {

  private String city;

  public OuterWithNullEmptyCity() {}

  public OuterWithNullEmptyCity(String city) {
    this.city = city;
  }

  public String getCity() {
    return city;
  }

  @JsonSetter(nulls = Nulls.AS_EMPTY)
  public void setCity(String city) {
    this.city = city;
  }
}
