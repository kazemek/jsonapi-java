package io.github.kazemek.jsonapi.testsupport.fixtures.compoundwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person;
import org.jspecify.annotations.Nullable;

/** Declared comment owner type for polymorphic include-policy tests. */
@JsonApiResource(type = "comments")
public class BaseComment {

  private @Nullable String id;
  private @Nullable String body;
  private @Nullable Person author;

  public BaseComment() {}

  public BaseComment(@Nullable String id, @Nullable String body, @Nullable Person author) {
    this.id = id;
    this.body = body;
    this.author = author;
  }

  @JsonApiId
  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  @JsonApiAttribute
  public @Nullable String getBody() {
    return body;
  }

  public void setBody(@Nullable String body) {
    this.body = body;
  }

  @JsonApiRelationship
  public @Nullable Person getAuthor() {
    return author;
  }

  public void setAuthor(@Nullable Person author) {
    this.author = author;
  }
}
