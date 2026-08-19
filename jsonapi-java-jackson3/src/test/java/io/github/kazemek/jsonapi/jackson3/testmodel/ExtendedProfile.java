package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;

/**
 * Concrete subtype of {@link BaseProfile} used as the {@code @JsonDeserialize(as = ...)} target on
 * the setter of a {@link BaseProfile}-typed member (ADR-014).
 */
public final class ExtendedProfile extends BaseProfile {

  private String email;

  public ExtendedProfile() {}

  public ExtendedProfile(String name, String email) {
    super(name);
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
    if (!(other instanceof ExtendedProfile that) || !super.equals(other)) {
      return false;
    }
    return Objects.equals(email, that.email);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), email);
  }
}
