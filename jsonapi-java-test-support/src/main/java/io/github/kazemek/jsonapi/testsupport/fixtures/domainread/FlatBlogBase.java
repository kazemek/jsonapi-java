package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

public class FlatBlogBase {

  @JsonApiId private @Nullable String id;

  private @Nullable String name;

  public FlatBlogBase() {}

  public FlatBlogBase(@Nullable String id, @Nullable String name) {
    this.id = id;
    this.name = name;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  @JsonApiAttribute
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
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    FlatBlogBase other = (FlatBlogBase) obj;
    return Objects.equals(id, other.id) && Objects.equals(name, other.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name);
  }
}
