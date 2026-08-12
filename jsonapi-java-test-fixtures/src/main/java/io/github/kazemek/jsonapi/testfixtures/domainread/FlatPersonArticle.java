package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "articles")
public class FlatPersonArticle {

  @JsonApiId private @Nullable String id;

  @JsonApiRelationship private @Nullable Person author;

  public FlatPersonArticle() {}

  public FlatPersonArticle(@Nullable String id, @Nullable Person author) {
    this.id = id;
    this.author = author;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  public @Nullable Person getAuthor() {
    return author;
  }

  public void setAuthor(@Nullable Person author) {
    this.author = author;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FlatPersonArticle other)) {
      return false;
    }
    return Objects.equals(id, other.id) && Objects.equals(author, other.author);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, author);
  }
}
