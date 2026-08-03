package io.github.kazemek.jsonapi.jackson3.testmodel;

import io.github.kazemek.jsonapi.annotation.JsonApiResource;

/** Runtime subtype whose JSON:API type differs from {@link BaseComment}. */
@JsonApiResource(type = "moderated-comments")
public final class ModeratedComment extends BaseComment {

  public ModeratedComment() {
    super();
  }

  public ModeratedComment(String id, String body, Person author) {
    super(id, body, author);
  }
}
