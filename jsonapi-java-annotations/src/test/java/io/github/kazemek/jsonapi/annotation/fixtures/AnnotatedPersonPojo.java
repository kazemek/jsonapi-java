package io.github.kazemek.jsonapi.annotation.fixtures;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

@JsonApiResource(type = "people")
public final class AnnotatedPersonPojo {

  @JsonApiId private final String id;

  @JsonApiAttribute private final String name;

  @JsonApiAttribute private final String email;

  @JsonApiRelationship private final String articleIds;

  @JsonApiRelationship private final String managerId;

  public AnnotatedPersonPojo(
      @JsonApiId String id,
      @JsonApiAttribute String name,
      @JsonApiAttribute String email,
      @JsonApiRelationship String articleIds,
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

  @JsonApiAttribute
  public String getName() {
    return name;
  }

  @JsonApiAttribute
  public String getEmail() {
    return email;
  }

  @JsonApiRelationship
  public String getArticleIds() {
    return articleIds;
  }

  @JsonApiRelationship
  public String getManagerId() {
    return managerId;
  }
}
