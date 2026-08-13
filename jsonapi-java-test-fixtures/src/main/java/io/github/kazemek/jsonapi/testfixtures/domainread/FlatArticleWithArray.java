package io.github.kazemek.jsonapi.testfixtures.domainread;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import org.jspecify.annotations.Nullable;

/**
 * Flat read-side DTO with an array-based to-many ResourceIdentifier relationship. Adapter suites
 * compare {@code comments} element-wise because the generated record {@code equals} compares array
 * components by reference.
 */
@JsonApiResource(type = "articles")
@SuppressWarnings({"ArrayRecordComponent", "java:S6218"})
public record FlatArticleWithArray(
    @JsonApiId String id,
    @Nullable String title,
    @JsonApiRelationship ResourceIdentifier @Nullable [] comments) {

  public FlatArticleWithArray {
    comments = comments == null ? null : comments.clone();
  }

  @Override
  public ResourceIdentifier @Nullable [] comments() {
    return comments == null ? null : comments.clone();
  }
}
