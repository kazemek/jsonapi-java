package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.RelationshipLinkage;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Supported {@link RelationshipLinkage} container shapes for identifier-meta write and read: array,
 * {@link Set}, {@link Optional}, {@link Map} identifier meta, and a renamed relationship wire name
 * (ADR-017).
 */
public final class RelationshipLinkageContainerFixtures {

  private RelationshipLinkageContainerFixtures() {}

  /**
   * Array to-many {@link RelationshipLinkage}. Record equality compares comments element-wise
   * because the generated record {@code equals} uses array identity.
   */
  @JsonApiResource(type = "articles")
  @SuppressWarnings({"ArrayRecordComponent", "java:S6218"})
  public record ArrayRelationshipLinkageArticle(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<ResourceIdentifier, CommentIdMeta>[] comments) {

    public ArrayRelationshipLinkageArticle {
      comments = Objects.requireNonNull(comments, "comments").clone();
    }

    @Override
    public RelationshipLinkage<ResourceIdentifier, CommentIdMeta>[] comments() {
      return comments.clone();
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof ArrayRelationshipLinkageArticle(String otherId, var otherComments))) {
        return false;
      }
      return Objects.equals(id, otherId) && Arrays.equals(comments, otherComments);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, Arrays.hashCode(comments));
    }

    @Override
    public String toString() {
      return "ArrayRelationshipLinkageArticle[id="
          + id
          + ", comments="
          + Arrays.toString(comments)
          + "]";
    }
  }

  @JsonApiResource(type = "articles")
  public record SetRelationshipLinkageArticle(
      @JsonApiId String id,
      @JsonApiRelationship Set<RelationshipLinkage<ResourceIdentifier, CommentIdMeta>> comments) {}

  @JsonApiResource(type = "articles")
  public record OptionalRelationshipLinkageArticle(
      @JsonApiId String id,
      @JsonApiRelationship
          Optional<RelationshipLinkage<ResourceIdentifier, AuthorIdMeta>> author) {}

  @JsonApiResource(type = "articles")
  public record MapRelationshipLinkageArticle(
      @JsonApiId String id,
      @JsonApiRelationship
          List<RelationshipLinkage<ResourceIdentifier, Map<String, Object>>> comments) {}

  @JsonApiResource(type = "articles")
  public record RenamedRelationshipLinkageArticle(
      @JsonApiId String id,
      @JsonApiRelationship(name = "author")
          @Nullable RelationshipLinkage<ResourceIdentifier, AuthorIdMeta> writtenBy) {}
}
