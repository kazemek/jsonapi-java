package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.jackson.PatchPresence;
import java.util.Objects;

/**
 * Presence-aware nested PATCH shape whose deeper member is a throwing {@link ThrowingGeoPatch},
 * used to prove deep Jackson construction-failure paths are translated to wire-name pointers
 * (ADR-014).
 */
public final class ThrowingAddressPatch {

  private PatchPresence<String> street;
  private PatchPresence<ThrowingGeoPatch> geo;

  public ThrowingAddressPatch() {}

  public ThrowingAddressPatch(PatchPresence<String> street, PatchPresence<ThrowingGeoPatch> geo) {
    this.street = street;
    this.geo = geo;
  }

  public PatchPresence<String> getStreet() {
    return street;
  }

  public void setStreet(PatchPresence<String> street) {
    this.street = street;
  }

  public PatchPresence<ThrowingGeoPatch> getGeo() {
    return geo;
  }

  public void setGeo(PatchPresence<ThrowingGeoPatch> geo) {
    this.geo = geo;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ThrowingAddressPatch that)) {
      return false;
    }
    return Objects.equals(street, that.street) && Objects.equals(geo, that.geo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(street, geo);
  }
}
