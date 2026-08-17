package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.jackson.PatchPresence;

/** Patch DTO with a raw PatchPresence property for negative projection scenarios. */
@JsonApiResource(type = "articles")
public class FlatRawPatchPresence {
  @JsonApiAttribute private PatchPresence title;

  public FlatRawPatchPresence(@JsonApiAttribute PatchPresence title) {
    this.title = title;
  }

  public PatchPresence getTitle() {
    return title;
  }
}
