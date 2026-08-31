package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "things")
public class FlatCountedThing {

  @JsonApiId private @Nullable String id;

  private int count;

  public FlatCountedThing() {}

  public FlatCountedThing(@Nullable String id, int count) {
    this.id = id;
    this.count = count;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  @JsonApiAttribute
  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FlatCountedThing other)) {
      return false;
    }
    return count == other.count && Objects.equals(id, other.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, count);
  }
}
