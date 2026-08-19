package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Ordinary structured domain value type with a bean-valued {@code details} property whose setter
 * carries a property-scoped {@code @JsonDeserialize}. The surrounding bean recurses while the
 * customized {@code details} member stays Atomic with the custom deserializer applied (ADR-014).
 */
public final class OuterWithSetterCustomDetails {

  private Details details;

  public OuterWithSetterCustomDetails() {}

  public OuterWithSetterCustomDetails(Details details) {
    this.details = details;
  }

  public Details getDetails() {
    return details;
  }

  @JsonDeserialize(using = CustomDetailsDeserializer.class)
  public void setDetails(Details details) {
    this.details = details;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof OuterWithSetterCustomDetails that)) {
      return false;
    }
    return Objects.equals(details, that.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(details);
  }
}
