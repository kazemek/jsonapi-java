package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;

/**
 * Concrete {@link Contact} subtype for the polymorphic low-level atomic-conversion fixture
 * (ADR-014).
 */
public final class EmailContact extends Contact {

  private String email;

  public EmailContact() {}

  public EmailContact(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof EmailContact that)) {
      return false;
    }
    return Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(email);
  }
}
