package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Non-record patch DTO for negative projection scenarios. */
@JsonApiResource(type = "articles")
public class FlatClassPatch {
  @JsonApiAttribute private PatchPresence<String> title;

  public FlatClassPatch(@JsonApiAttribute PatchPresence<String> title) {
    this.title = title;
  }

  public PatchPresence<String> getTitle() {
    return title;
  }
}
