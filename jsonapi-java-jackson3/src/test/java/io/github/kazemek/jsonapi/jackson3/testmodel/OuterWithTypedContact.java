package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;

/**
 * Ordinary structured domain value type with a polymorphic {@link Contact} member, proving a
 * property-level {@code TypeDeserializer} path is preserved through the low-level atomic conversion
 * ({@code SettableBeanProperty.deserialize}) (ADR-014).
 */
public final class OuterWithTypedContact {

  private Contact contact;

  public OuterWithTypedContact() {}

  public OuterWithTypedContact(Contact contact) {
    this.contact = contact;
  }

  public Contact getContact() {
    return contact;
  }

  public void setContact(Contact contact) {
    this.contact = contact;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof OuterWithTypedContact that)) {
      return false;
    }
    return Objects.equals(contact, that.contact);
  }

  @Override
  public int hashCode() {
    return Objects.hash(contact);
  }
}
