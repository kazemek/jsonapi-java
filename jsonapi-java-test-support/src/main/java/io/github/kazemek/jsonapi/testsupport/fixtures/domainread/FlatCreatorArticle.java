package io.github.kazemek.jsonapi.testsupport.fixtures.domainread;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** Immutable flat read-side DTO constructed through an explicit {@code @JsonCreator}. */
@JsonApiResource(type = "articles")
public final class FlatCreatorArticle {

  private final @Nullable String id;
  private final @Nullable String title;

  @JsonCreator
  public FlatCreatorArticle(
      @JsonProperty("id") @JsonApiId @Nullable String id,
      @JsonProperty("title") @JsonApiAttribute @Nullable String title) {
    this.id = id;
    this.title = title;
  }

  public @Nullable String getId() {
    return id;
  }

  public @Nullable String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof FlatCreatorArticle other)) {
      return false;
    }
    return Objects.equals(id, other.id) && Objects.equals(title, other.title);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title);
  }
}
