package io.github.kazemek.jsonapi.jackson3;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationshipMeta;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier;
import io.github.kazemek.jsonapi.jackson.PatchPresence;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMeta;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMetaPatch;
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorMeta;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Whole-meta target declaration shapes owned by {@code FlatMetaMappingSpec} (ADR-015): valid and
 * invalid {@code @JsonApiMeta} target declarations across write/read/low-level-PATCH/typed-PATCH
 * paths, including duplicates, unmapped relationship meta, scalar/list/JDK-scalar rejections,
 * Object/Map targets, missing-meta-member PATCH DTOs, renamed wire names, and root-polymorphic
 * TypeDeserializer targets.
 */
public final class WholeMetaTargetFixtures {

  private WholeMetaTargetFixtures() {}

  /** Write/low-level model with a renamed relationship whose meta references the wire name. */
  @JsonApiResource(type = "articles")
  public record RenamedRelationshipMetaArticle(
      @JsonApiId String id,
      @JsonApiAttribute String title,
      @JsonApiRelationship(name = "author") ResourceIdentifier writtenBy,
      @JsonApiMeta ArticleMeta meta,
      @JsonApiRelationshipMeta("author") AuthorMeta authorMeta) {}

  /** Invalid declaration: two resource meta properties on one mapping. */
  @JsonApiResource(type = "articles")
  public record DuplicateMetaArticle(
      @JsonApiId String id, @JsonApiMeta String meta, @JsonApiMeta String otherMeta) {}

  /** Invalid declaration: two relationship meta properties targeting the same relationship. */
  @JsonApiResource(type = "articles")
  public record DuplicateRelationshipMetaArticle(
      @JsonApiId String id,
      @JsonApiRelationship ResourceIdentifier author,
      @JsonApiRelationshipMeta("author") String authorMeta,
      @JsonApiRelationshipMeta("author") String authorMeta2) {}

  /** Invalid declaration: relationship meta referencing an unmapped relationship. */
  @JsonApiResource(type = "articles")
  public record UnmappedRelationshipMetaArticle(
      @JsonApiId String id, @JsonApiRelationshipMeta("nonexistent") AuthorMeta authorMeta) {}

  /** Invalid declaration: scalar whole-meta target. */
  @JsonApiResource(type = "articles")
  public record ScalarMetaArticle(@JsonApiId String id, @JsonApiMeta String meta) {}

  /** Invalid typed PATCH declaration: PatchPresence wrapping a scalar meta target. */
  @JsonApiResource(type = "articles")
  public record ScalarMetaPatch(
      @JsonApiId String id,
      @JsonApiAttribute PatchPresence<String> title,
      @JsonApiMeta PatchPresence<String> meta) {}

  /** Invalid declaration: list whole-meta target. */
  @JsonApiResource(type = "articles")
  public record ListMetaArticle(@JsonApiId String id, @JsonApiMeta List<String> meta) {}

  /**
   * Object-typed resource meta target: declared-valid, but the runtime value must be map-shaped.
   */
  @JsonApiResource(type = "articles")
  public record ObjectMetaArticle(@JsonApiId String id, @JsonApiMeta Object meta) {}

  /** Typed PATCH model with an explicit {@code PatchPresence<Object>} whole-meta target. */
  @JsonApiResource(type = "articles")
  public record ObjectMetaPatch(@JsonApiId String id, @JsonApiMeta PatchPresence<Object> meta) {}

  /** Invalid declaration: UUID whole-meta target (scalar, not bean-shaped). */
  @JsonApiResource(type = "articles")
  public record UuidMetaArticle(@JsonApiId String id, @JsonApiMeta UUID meta) {}

  /** Invalid typed PATCH declaration: PatchPresence wrapping a UUID scalar meta target. */
  @JsonApiResource(type = "articles")
  public record UuidMetaPatch(
      @JsonApiId String id,
      @JsonApiAttribute PatchPresence<String> title,
      @JsonApiMeta PatchPresence<UUID> meta) {}

  /** Invalid declaration: java.time whole-meta target (scalar, not bean-shaped). */
  @JsonApiResource(type = "articles")
  public record InstantMetaArticle(@JsonApiId String id, @JsonApiMeta Instant meta) {}

  /** Invalid declaration: URI whole-meta target (scalar, not bean-shaped). */
  @JsonApiResource(type = "articles")
  public record UriMetaArticle(@JsonApiId String id, @JsonApiMeta URI meta) {}

  /** Invalid typed PATCH declaration: nested PatchPresence wrapper chain for meta. */
  @JsonApiResource(type = "articles")
  public record NestedPresenceMetaPatch(
      @JsonApiId String id,
      @JsonApiAttribute PatchPresence<String> title,
      @JsonApiMeta PatchPresence<PatchPresence<String>> meta) {}

  /** Typed PATCH DTO without any meta member: supplied meta must be rejected (ADR-015). */
  @JsonApiResource(type = "articles")
  public record NoMetaPatch(
      @JsonApiId String id,
      @JsonApiAttribute PatchPresence<String> title,
      @JsonApiRelationship PatchPresence<ResourceIdentifier> author) {}

  /**
   * Typed PATCH DTO with resource meta but no relationship meta member: supplied rel meta rejected.
   */
  @JsonApiResource(type = "articles")
  public record NoRelMetaPatch(
      @JsonApiId String id,
      @JsonApiAttribute PatchPresence<String> title,
      @JsonApiRelationship PatchPresence<ResourceIdentifier> author,
      @JsonApiMeta PatchPresence<ArticleMetaPatch> meta) {}

  /**
   * Concrete POJO carrying root {@code @JsonTypeInfo}; its root deserializer is wrapped by a {@code
   * TypeDeserializer} (decorated), yet it is still an object-shaped whole-meta target (ADR-015).
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
  @JsonSubTypes({@JsonSubTypes.Type(value = ConcreteTypedMeta.class, name = "concrete")})
  public static class ConcreteTypedMeta {

    private String value;

    public ConcreteTypedMeta() {}

    public ConcreteTypedMeta(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof ConcreteTypedMeta that)) {
        return false;
      }
      return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(value);
    }
  }

  /** Low-level domain model with a concrete root-polymorphic whole-meta target. */
  @JsonApiResource(type = "articles")
  public record ConcreteTypedMetaArticle(
      @JsonApiId String id, @JsonApiMeta ConcreteTypedMeta meta) {}

  /**
   * Abstract polymorphic whole-meta base type: Jackson materializes the concrete subtype from the
   * {@code kind} discriminator through the property/root {@code TypeDeserializer} (ADR-015).
   */
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "kind")
  @JsonSubTypes({@JsonSubTypes.Type(value = SourceMeta.class, name = "source")})
  public abstract static class PolyMetaBase {

    private String note;

    public PolyMetaBase() {}

    public PolyMetaBase(String note) {
      this.note = note;
    }

    public String getNote() {
      return note;
    }

    public void setNote(String note) {
      this.note = note;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof PolyMetaBase that)) {
        return false;
      }
      return Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
      return Objects.hash(note);
    }
  }

  /** Concrete {@link PolyMetaBase} subtype selected from the {@code kind} discriminator. */
  public static final class SourceMeta extends PolyMetaBase {

    private String source;

    public SourceMeta() {}

    public SourceMeta(String source, String note) {
      super(note);
      this.source = source;
    }

    public String getSource() {
      return source;
    }

    public void setSource(String source) {
      this.source = source;
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof SourceMeta that)) {
        return false;
      }
      return Objects.equals(source, that.source) && super.equals(that);
    }

    @Override
    public int hashCode() {
      return Objects.hash(source, super.hashCode());
    }
  }

  /** Low-level domain model with an abstract polymorphic whole-meta target. */
  @JsonApiResource(type = "articles")
  public record PolyMetaArticle(@JsonApiId String id, @JsonApiMeta PolyMetaBase meta) {}

  /** Typed PATCH model with a presence-aware abstract polymorphic whole-meta target. */
  @JsonApiResource(type = "articles")
  public record PolyMetaArticlePatch(
      @JsonApiId String id, @JsonApiMeta PatchPresence<PolyMetaBase> meta) {}
}
