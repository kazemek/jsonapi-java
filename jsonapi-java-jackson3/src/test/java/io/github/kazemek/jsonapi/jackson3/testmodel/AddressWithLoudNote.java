package io.github.kazemek.jsonapi.jackson3.testmodel;

import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Ordinary non-record structured domain value type with a property-level {@code @JsonDeserialize}
 * on a JavaBean field. Proves the low-level path treats such a member as atomic (rather than
 * traversing it) while still honoring the property-scoped deserializer during nested atomic
 * conversion (ADR-014).
 */
public final class AddressWithLoudNote {

  private String street;

  @JsonDeserialize(using = UpperCaseStringDeserializer.class)
  private String note;

  public AddressWithLoudNote() {}

  public AddressWithLoudNote(String street, String note) {
    this.street = street;
    this.note = note;
  }

  public String getStreet() {
    return street;
  }

  public void setStreet(String street) {
    this.street = street;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
