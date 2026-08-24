package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "things")
public class FlatThingWithIgnored {

  @JsonApiId private @Nullable String id;

  @JsonIgnore
  @JsonApiAttribute(name = "secret")
  private @Nullable String confidential;

  private @Nullable String name;

  public FlatThingWithIgnored() {}

  public FlatThingWithIgnored(
      @Nullable String id, @Nullable String name, @Nullable String confidential) {
    this.id = id;
    this.name = name;
    this.confidential = confidential;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  public @Nullable String getConfidential() {
    return confidential;
  }

  public void setConfidential(@Nullable String confidential) {
    this.confidential = confidential;
  }

  public @Nullable String getName() {
    return name;
  }

  public void setName(@Nullable String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FlatThingWithIgnored other)) {
      return false;
    }
    return Objects.equals(id, other.id)
        && Objects.equals(name, other.name)
        && Objects.equals(confidential, other.confidential);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, confidential);
  }
}
