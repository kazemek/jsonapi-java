package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.List;
import org.jspecify.annotations.Nullable;

@JsonApiResource(type = "pojos")
public class SamplePojo {

  @JsonApiId private @Nullable String id;

  @JsonApiAttribute
  @JsonProperty("display-name")
  private @Nullable String name;

  @JsonApiRelationship private @Nullable List<Comment> comments;

  public SamplePojo() {}

  public SamplePojo(@Nullable String id, @Nullable String name, @Nullable List<Comment> comments) {
    this.id = id;
    this.name = name;
    this.comments = comments;
  }

  public @Nullable String getId() {
    return id;
  }

  public void setId(@Nullable String id) {
    this.id = id;
  }

  public @Nullable String getName() {
    return name;
  }

  public void setName(@Nullable String name) {
    this.name = name;
  }

  public @Nullable List<Comment> getComments() {
    return comments;
  }

  public void setComments(@Nullable List<Comment> comments) {
    this.comments = comments;
  }
}
