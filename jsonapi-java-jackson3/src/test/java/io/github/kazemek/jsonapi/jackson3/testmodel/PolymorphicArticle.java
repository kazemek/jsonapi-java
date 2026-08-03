package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;

/** Article whose comments relationship is declared as {@link BaseComment}. */
@JsonApiResource(type = "articles")
public final class PolymorphicArticle {

  private final String id;
  private final String title;
  private final List<BaseComment> comments;

  public PolymorphicArticle(String id, String title, List<BaseComment> comments) {
    this.id = id;
    this.title = title;
    this.comments = comments;
  }

  @JsonApiId
  public String getId() {
    return id;
  }

  @JsonApiAttribute
  public String getTitle() {
    return title;
  }

  @JsonApiRelationship
  public List<BaseComment> getComments() {
    return comments;
  }
}
