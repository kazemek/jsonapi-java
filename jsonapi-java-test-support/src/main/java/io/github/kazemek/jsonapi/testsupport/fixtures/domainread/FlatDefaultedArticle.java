package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "articles")
public class FlatDefaultedArticle {

  @JsonApiId private @Nullable String id;

  private @Nullable String title = "default";

  private String body = "default";

  public FlatDefaultedArticle() {}

  public FlatDefaultedArticle(@Nullable String id, @Nullable String title, String body) {
    this.id = id;
    this.title = title;
    this.body = body;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  public @Nullable String getTitle() {
    return title;
  }

  public void setTitle(@Nullable String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FlatDefaultedArticle other)) {
      return false;
    }
    return Objects.equals(id, other.id)
        && Objects.equals(title, other.title)
        && Objects.equals(body, other.body);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, body);
  }
}
