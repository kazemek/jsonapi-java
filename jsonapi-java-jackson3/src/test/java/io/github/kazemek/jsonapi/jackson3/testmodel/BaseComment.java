package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.Person;

/** Declared comment owner type for polymorphic include-policy tests. */
@JsonApiResource(type = "comments")
public class BaseComment {

  private String id;
  private String body;
  private Person author;

  public BaseComment() {}

  public BaseComment(String id, String body, Person author) {
    this.id = id;
    this.body = body;
    this.author = author;
  }

  @JsonApiId
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  @JsonApiRelationship
  public Person getAuthor() {
    return author;
  }

  public void setAuthor(Person author) {
    this.author = author;
  }
}
