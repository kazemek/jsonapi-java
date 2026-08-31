package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "articles")
public class FlatMutableArticle {

  @JsonApiId private @Nullable String id;

  private @Nullable String title;

  @JsonApiRelationship private @Nullable ResourceIdentifier author;

  public FlatMutableArticle() {}

  public FlatMutableArticle(
      @Nullable String id, @Nullable String title, @Nullable ResourceIdentifier author) {
    this.id = id;
    this.title = title;
    this.author = author;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  @JsonApiAttribute
  public @Nullable String getTitle() {
    return title;
  }

  public void setTitle(@Nullable String title) {
    this.title = title;
  }

  public @Nullable ResourceIdentifier getAuthor() {
    return author;
  }

  public void setAuthor(@Nullable ResourceIdentifier author) {
    this.author = author;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FlatMutableArticle other)) {
      return false;
    }
    return Objects.equals(id, other.id)
        && Objects.equals(title, other.title)
        && Objects.equals(author, other.author);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, author);
  }
}
