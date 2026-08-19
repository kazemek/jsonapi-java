package io.github.kazemek.jsonapi.jackson3.testmodel;

import java.util.Objects;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Ordinary structured domain value type with a {@link BaseProfile}-typed {@code profile} member
 * whose setter carries {@code @JsonDeserialize(as = ExtendedProfile.class)}. The refinement is a
 * type-refinement customization that must be detected through the resolved property type (a setter
 * {@code AnnotatedMethod.getType()} is {@code void}, which makes refinement checks against it
 * incorrect), keeping the member Atomic with the refined deserializer applied instead of recursing
 * (ADR-014).
 */
public final class OuterWithSetterAsProfile {

  private BaseProfile profile;

  public OuterWithSetterAsProfile() {}

  public OuterWithSetterAsProfile(BaseProfile profile) {
    this.profile = profile;
  }

  public BaseProfile getProfile() {
    return profile;
  }

  @JsonDeserialize(as = ExtendedProfile.class)
  public void setProfile(BaseProfile profile) {
    this.profile = profile;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof OuterWithSetterAsProfile that)) {
      return false;
    }
    return Objects.equals(profile, that.profile);
  }

  @Override
  public int hashCode() {
    return Objects.hash(profile);
  }
}
