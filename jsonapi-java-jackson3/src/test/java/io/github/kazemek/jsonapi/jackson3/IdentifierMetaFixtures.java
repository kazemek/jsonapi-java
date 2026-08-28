package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.RelationshipLinkage;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Identifier-meta conversion fixtures owned by {@code IdentifierMetaMappingSpec} (ADR-017).
 * Declaration and write-failure carriers live in the shared write-diagnostics catalog.
 */
public final class IdentifierMetaFixtures {

  private IdentifierMetaFixtures() {}

  /** Generic structured value used to prove identifier-meta {@code JavaType} preservation. */
  public record IdMetaBox<T>(T value) {}

  @JsonApiResource(type = "articles")
  public record GenericIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<ResourceIdentifier, IdMetaBox<Integer>> author) {}

  @JsonApiResource(type = "articles")
  @SuppressWarnings({"ArrayRecordComponent", "java:S6218"})
  public record ArrayIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<ResourceIdentifier, CommentIdMeta>[] comments) {}

  @JsonApiResource(type = "articles")
  public record SetIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship Set<RelationshipLinkage<ResourceIdentifier, CommentIdMeta>> comments) {}

  @JsonApiResource(type = "articles")
  public record OptionalIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship
          Optional<RelationshipLinkage<ResourceIdentifier, AuthorIdMeta>> author) {}

  @JsonApiResource(type = "articles")
  public record MapMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship
          List<RelationshipLinkage<ResourceIdentifier, Map<String, Object>>> comments) {}

  @JsonApiResource(type = "articles")
  public record SnakeIdentifierMeta(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<ResourceIdentifier, SnakeIdMeta> author) {}

  public record SnakeIdMeta(String displayRole) {}

  @JsonApiResource(type = "articles")
  public record RenamedRelationshipIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship(name = "author")
          RelationshipLinkage<ResourceIdentifier, AuthorIdMeta> writtenBy) {}

  @JsonApiResource(type = "articles")
  public record SerializedIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<ResourceIdentifier, EncodedIdMeta> author) {}

  @JsonSerialize(using = EncodedIdMetaSerializer.class)
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
  public record NonEmittingIdentifierMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<ResourceIdentifier, SilentIdMeta> author) {}

  @JsonSerialize(using = SilentIdMetaSerializer.class)
  public record SilentIdMeta(String role) {}

  public static final class SilentIdMetaSerializer extends ValueSerializer<SilentIdMeta> {
    @Override
    public void serialize(
        SilentIdMeta value, JsonGenerator generator, SerializationContext context) {
      // Emit nothing so overlay is skipped and existing identifier meta is preserved.
    }
  }

  @JsonApiResource(type = "articles")
  public record ScalarSerializedMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<ResourceIdentifier, ScalarIdMeta> author) {}

  @JsonSerialize(using = ScalarIdMetaSerializer.class)
  public record ScalarIdMeta(String role) {}

  public static final class ScalarIdMetaSerializer extends ValueSerializer<ScalarIdMeta> {
    @Override
    public void serialize(
        ScalarIdMeta value, JsonGenerator generator, SerializationContext context) {
      generator.writeString(value.role());
    }
  }

  @JsonApiResource(type = "articles")
  public record WrappedDomainArticle(
      @JsonApiId String id,
      @JsonApiRelationship RelationshipLinkage<Person, AuthorIdMeta> author,
      @JsonApiRelationship List<RelationshipLinkage<Comment, CommentIdMeta>> comments) {}

  @JsonApiResource(type = "articles")
  public record WrappedMappedArticle(
      @JsonApiId String id,
      @JsonApiRelationship
          RelationshipLinkage<LinkageMapperFixtures.FlatAuthor, AuthorIdMeta> author) {}

  @JsonApiResource(type = "articles")
  public record WrappedMappedSetArticle(
      @JsonApiId String id,
      @JsonApiRelationship
          Set<RelationshipLinkage<LinkageMapperFixtures.FlatAuthor, CommentIdMeta>> comments) {}
}
