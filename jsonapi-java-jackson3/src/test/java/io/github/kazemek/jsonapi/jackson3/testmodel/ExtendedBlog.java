package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "blogs")
public final class ExtendedBlog extends BaseBlog {

  private String description;

  public ExtendedBlog() {
    super();
  }

  public ExtendedBlog(String id, String name, String description) {
    super(id, name);
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
