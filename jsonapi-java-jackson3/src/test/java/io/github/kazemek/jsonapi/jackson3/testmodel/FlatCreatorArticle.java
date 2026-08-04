package io.github.kazemek.jsonapi.jackson3.testmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Immutable flat read-side DTO constructed through an explicit @JsonCreator. */
@JsonApiResource(type = "articles")
public final class FlatCreatorArticle {

  private final String id;
  private final String title;

  @JsonCreator
  public FlatCreatorArticle(
      @JsonProperty("id") @JsonApiId String id, @JsonProperty("title") String title) {
    this.id = id;
    this.title = title;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }
}
