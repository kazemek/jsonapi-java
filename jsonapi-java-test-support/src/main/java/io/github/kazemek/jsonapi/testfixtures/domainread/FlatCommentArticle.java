package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Comment;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "articles")
public class FlatCommentArticle {

  @JsonApiId private @Nullable String id;

  @JsonApiRelationship private @Nullable List<Comment> comments;

  public FlatCommentArticle() {}

  public FlatCommentArticle(@Nullable String id, @Nullable List<Comment> comments) {
    this.id = id;
    this.comments = comments;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  public @Nullable List<Comment> getComments() {
    return comments;
  }

  public void setComments(@Nullable List<Comment> comments) {
    this.comments = comments;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FlatCommentArticle other)) {
      return false;
    }
    return Objects.equals(id, other.id) && Objects.equals(comments, other.comments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, comments);
  }
}
