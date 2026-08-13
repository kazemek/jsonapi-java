package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "blogs")
public class FlatInheritedBlog extends FlatBlogBase {

  private @Nullable String description;

  public FlatInheritedBlog() {}

  public FlatInheritedBlog(
      @Nullable String id, @Nullable String name, @Nullable String description) {
    super(id, name);
    this.description = description;
  }

  public @Nullable String getDescription() {
    return description;
  }

  public void setDescription(@Nullable String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    FlatInheritedBlog other = (FlatInheritedBlog) obj;
    return Objects.equals(getId(), other.getId())
        && Objects.equals(getName(), other.getName())
        && Objects.equals(description, other.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName(), description);
  }
}
