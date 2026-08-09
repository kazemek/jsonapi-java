package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Flat read-side DTO whose canonical constructor rejects one attribute value. */
@JsonApiResource(type = "throwing-articles")
public record FlatThrowingArticle(@JsonApiId String id, @JsonApiAttribute String title) {

  public FlatThrowingArticle {
    if (title != null && title.equals("boom")) {
      throw new IllegalArgumentException("creator rejected value");
    }
  }
}
