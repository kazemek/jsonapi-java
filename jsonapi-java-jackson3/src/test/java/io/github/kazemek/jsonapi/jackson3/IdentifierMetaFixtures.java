package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiIdentifierMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Identifier-meta declaration and conversion fixtures owned by {@code IdentifierMetaMappingSpec}
 * (ADR-017).
 */
public final class IdentifierMetaFixtures {

  private IdentifierMetaFixtures() {}

  /** Generic structured value used to prove identifier-meta {@code JavaType} preservation. */
  public record IdMetaBox<T>(T value) {}

  @JsonApiResource(type = "articles")
  public record GenericIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiIdentifierMeta("author") IdMetaBox<Integer> authorIdMeta) {}

  @JsonApiResource(type = "articles")
  @SuppressWarnings({"ArrayRecordComponent", "java:S6218"})
  public record ArrayIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship List<ResourceIdentifier> comments,
      @JsonApiIdentifierMeta("comments") CommentIdMeta[] commentIdMetas) {}

  @JsonApiResource(type = "articles")
  public record SnakeIdentifierMeta(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiIdentifierMeta("author") SnakeIdMeta authorIdMeta) {}

  public record SnakeIdMeta(String displayRole) {}

  @JsonApiResource(type = "articles")
  public record RenamedRelationshipIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship(name = "author") ResourceIdentifier writtenBy,
      @JsonApiIdentifierMeta("author") AuthorIdMeta authorIdMeta) {}

  @JsonApiResource(type = "articles")
  public record SerializedIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiIdentifierMeta("author") @JsonSerialize(using = EncodedIdMetaSerializer.class)
          EncodedIdMeta authorIdMeta) {}

  public record EncodedIdMeta(String role) {}

  public static final class EncodedIdMetaSerializer extends ValueSerializer<EncodedIdMeta> {
    @Override
    public void serialize(
        EncodedIdMeta value, JsonGenerator generator, SerializationContext context) {
      generator.writeStartObject();
      generator.writeName("encoded");
      generator.writeString(value.role());
      generator.writeEndObject();
    }
  }

  @JsonApiResource(type = "articles")
  public record DuplicateIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiIdentifierMeta("author") AuthorIdMeta first,
      @JsonApiIdentifierMeta("author") AuthorIdMeta second) {}

  @JsonApiResource(type = "articles")
  public record UnmappedIdentifierMetaArticle(
      @JsonApiId String id, @JsonApiIdentifierMeta("nonexistent") AuthorIdMeta authorIdMeta) {}

  @JsonApiResource(type = "articles")
  public record ScalarIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiIdentifierMeta("author") String authorIdMeta) {}

  @JsonApiResource(type = "articles")
  public record SetIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship List<ResourceIdentifier> comments,
      @JsonApiIdentifierMeta("comments") Set<CommentIdMeta> commentIdMetas) {}

  @JsonApiResource(type = "articles")
  public record MapIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship List<ResourceIdentifier> comments,
      @JsonApiIdentifierMeta("comments") Map<String, CommentIdMeta> commentIdMetas) {}

  @JsonApiResource(type = "articles")
  public record ListOnToOneIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiIdentifierMeta("author") List<AuthorIdMeta> authorIdMeta) {}

  @JsonApiResource(type = "articles")
  public record BeanOnToManyIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship List<ResourceIdentifier> comments,
      @JsonApiIdentifierMeta("comments") CommentIdMeta commentIdMetas) {}

  @JsonApiResource(type = "articles")
  public record EmptyNameIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiIdentifierMeta("") AuthorIdMeta authorIdMeta) {}

  @JsonApiResource(type = "articles")
  public record LengthMismatchIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship List<ResourceIdentifier> comments,
      @JsonApiIdentifierMeta("comments") List<CommentIdMeta> commentIdMetas) {}
}
