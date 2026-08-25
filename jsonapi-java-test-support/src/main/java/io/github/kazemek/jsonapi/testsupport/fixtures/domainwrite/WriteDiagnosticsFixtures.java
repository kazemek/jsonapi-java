package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Passive declaration-shape carriers for the shared write-diagnostics catalog: deliberately
 * mis-declared resources (missing annotations, duplicate roles, name collisions, invalid or
 * reserved names), throwing accessors, write-only properties, and unsupported relationship value
 * shapes. Adapter suites map instances of these carriers through their own resource writer and
 * assert the shared semantic diagnostic categories and wire locations.
 */
public final class WriteDiagnosticsFixtures {

  private WriteDiagnosticsFixtures() {}

  /** Resource declaration with an empty {@code type} value. */
  @JsonApiResource(type = "")
  public record EmptyTypeEntity(@JsonApiId String id) {}

  /** Resource declaration whose {@code type} contains characters JSON:API forbids. */
  @JsonApiResource(type = "bad type!")
  public record InvalidTypeEntity(@JsonApiId String id) {}

  /** Annotated resource without any identifier property. */
  @JsonApiResource(type = "entities")
  public record NoIdEntity(String name) {}

  /** Annotated resource whose identifier property holds null. */
  @JsonApiResource(type = "entities")
  public record NullIdEntity(@JsonApiId @Nullable String id) {}

  /** Identifier member carrying a second role annotation. */
  @JsonApiResource(type = "dup")
  public record DuplicateRoleEntity(@JsonApiId @JsonApiAttribute String id) {}

  /** Attribute and relationship mapped onto the same member name. */
  @JsonApiResource(type = "collision")
  public record NameCollisionEntity(
      @JsonApiId String id,
      @JsonApiAttribute(name = "same") String fieldA,
      @JsonApiRelationship(name = "same") String fieldB) {}

  /** Two attributes mapped onto the same wire name. */
  @JsonApiResource(type = "dup-attrs")
  public record DuplicateAttrNameEntity(
      @JsonApiId String id,
      @JsonApiAttribute(name = "same") String fieldA,
      @JsonApiAttribute(name = "same") String fieldB) {}

  /** Two relationships mapped onto the same wire name. */
  @JsonApiResource(type = "dup-rels")
  public record DuplicateRelNameEntity(
      @JsonApiId String id,
      @JsonApiRelationship(name = "same") String otherA,
      @JsonApiRelationship(name = "same") String otherB) {}

  /** Attribute override containing characters JSON:API forbids. */
  @JsonApiResource(type = "invalid")
  public record InvalidAttrNameEntity(
      @JsonApiId String id, @JsonApiAttribute(name = "bad name!") String value) {}

  /** Attribute override using the reserved {@code type} member name. */
  @JsonApiResource(type = "reserved-attr")
  public record ReservedAttrNameEntity(
      @JsonApiId String id, @JsonApiAttribute(name = "type") String value) {}

  /** Relationship override containing characters JSON:API forbids. */
  @JsonApiResource(type = "invalid-rel")
  public record InvalidRelNameEntity(
      @JsonApiId String id, @JsonApiRelationship(name = "bad name!") String other) {}

  /** Relationship override using the reserved {@code id} member name. */
  @JsonApiResource(type = "reserved-rel")
  public record ReservedRelNameEntity(
      @JsonApiId String id, @JsonApiRelationship(name = "id") String other) {}

  /** Attribute getter that always throws. */
  @JsonApiResource(type = "failing-attr")
  public record FailingAttrEntity(@JsonApiId String id, @JsonApiAttribute String badAttr) {
    @Override
    public String badAttr() {
      throw new IllegalStateException("attribute read failure");
    }
  }

  /** Renamed attribute getter that always throws; failures must report the wire name. */
  @JsonApiResource(type = "renamed-failing-attr")
  public record RenamedFailingAttrEntity(
      @JsonApiId String id, @JsonApiAttribute(name = "body-text") String badAttr) {
    @Override
    public String badAttr() {
      throw new IllegalStateException("attribute read failure");
    }
  }

  /** Identifier getter that always throws. */
  @JsonApiResource(type = "failing-id")
  public record FailingIdEntity(@JsonApiId String id) {
    @Override
    public String id() {
      throw new IllegalStateException("id read failure");
    }
  }

  /** Annotated property with only a setter and no readable accessor. */
  @JsonApiResource(type = "write-only")
  public static final class MissingAccessorEntity {
    @JsonApiId private final String id;
    private @Nullable String secret;

    public MissingAccessorEntity(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    @JsonApiAttribute
    public void setSecret(@Nullable String secret) {
      this.secret = secret;
    }
  }

  /** To-many relationship declared as an unsupported runtime array type. */
  @JsonApiResource(type = "raw-array-rel")
  public record RenamedArrayRelEntity(
      @JsonApiId String id, @JsonApiRelationship(name = "ext-values") long[] values) {
    @Override
    public boolean equals(Object obj) {
      return obj instanceof RenamedArrayRelEntity(String otherId, long[] otherValues)
          && Objects.equals(id, otherId)
          && Arrays.equals(values, otherValues);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, Arrays.hashCode(values));
    }

    @Override
    public String toString() {
      return "RenamedArrayRelEntity[id=" + id + ", values=" + Arrays.toString(values) + "]";
    }
  }

  /** To-many relationship holding mixed element types. */
  @JsonApiResource(type = "mixed-rel")
  public record RenamedMixedRelEntity(
      @JsonApiId String id, @JsonApiRelationship(name = "ext-items") List<Object> items) {}

  /** Raw erased iterable: to-many by declaration with no resolvable content type. */
  public static final class RawBag implements Iterable<Object> {
    private final List<Object> items;

    public RawBag(Object... items) {
      this.items = Arrays.asList(items);
    }

    @Override
    public Iterator<Object> iterator() {
      return items.iterator();
    }
  }

  /** To-many relationship declared with an unresolvable erased iterable type. */
  @JsonApiResource(type = "bag-rel")
  public record RenamedBagRelEntity(
      @JsonApiId String id, @JsonApiRelationship(name = "ext-bag") RawBag things) {}
}
