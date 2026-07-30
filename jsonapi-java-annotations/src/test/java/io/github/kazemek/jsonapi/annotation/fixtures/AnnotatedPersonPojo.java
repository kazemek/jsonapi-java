package io.github.kazemek.jsonapi.annotation.fixtures;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "people")
public final class AnnotatedPersonPojo {

  @JsonApiId private final String id;

  @JsonApiAttribute(name = "full-name")
  private final String name;

  @JsonApiAttribute private final String email;

  @JsonApiRelationship(name = "articles")
  private final String articleIds;

  @JsonApiRelationship private final String managerId;

  public AnnotatedPersonPojo(
      @JsonApiId String id,
      @JsonApiAttribute(name = "full-name") String name,
      @JsonApiAttribute String email,
      @JsonApiRelationship(name = "articles") String articleIds,
      @JsonApiRelationship String managerId) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.articleIds = articleIds;
    this.managerId = managerId;
  }

  @JsonApiId
  public String getId() {
    return id;
  }

  @JsonApiAttribute(name = "full-name")
  public String getName() {
    return name;
  }

  @JsonApiAttribute
  public String getEmail() {
    return email;
  }

  @JsonApiRelationship(name = "articles")
  public String getArticleIds() {
    return articleIds;
  }

  @JsonApiRelationship
  public String getManagerId() {
    return managerId;
  }
}
