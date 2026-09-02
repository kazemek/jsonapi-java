package io.github.kazemek.jsonapi.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import org.jspecify.annotations.Nullable;

/**
 * JavaBean inheritance on the write path: mapped properties declared on a non-resource base type
 * plus a subclass-owned member (ADR-004 ordinary bean semantics).
 */
public final class InheritedBlogFixtures {

  private InheritedBlogFixtures() {}

  public abstract static class BaseBlog {

    protected @Nullable String id;
    protected @Nullable String name;

    protected BaseBlog() {}

    protected BaseBlog(@Nullable String id, @Nullable String name) {
      this.id = id;
      this.name = name;
    }

    @JsonApiId
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
  }

  @JsonApiResource(type = "blogs")
  public static final class ExtendedBlog extends BaseBlog {

    private @Nullable String description;

    public ExtendedBlog() {}

    public ExtendedBlog(@Nullable String id, @Nullable String name, @Nullable String description) {
      super(id, name);
      this.description = description;
    }

    @JsonApiAttribute
    public @Nullable String getDescription() {
      return description;
    }

    public void setDescription(@Nullable String description) {
      this.description = description;
    }
  }
}
