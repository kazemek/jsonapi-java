package io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite;

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute;
import io.github.kazemek.jsonapi.annotation.JsonApiId;
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship;
import io.github.kazemek.jsonapi.annotation.JsonApiResource;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
  public static final class EmptyTypeEntity {
    @JsonApiId private final String id;

    public EmptyTypeEntity(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  /** Resource declaration whose {@code type} contains characters JSON:API forbids. */
  @JsonApiResource(type = "bad type!")
  public static final class InvalidTypeEntity {
    @JsonApiId private final String id;

    public InvalidTypeEntity(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  /** Annotated resource without any identifier property. */
  @JsonApiResource(type = "entities")
  public static final class NoIdEntity {
    private final String name;

    public NoIdEntity(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  /** Annotated resource whose identifier property holds null. */
  @JsonApiResource(type = "entities")
  public static final class NullIdEntity {
    @JsonApiId private final @org.jspecify.annotations.Nullable String id;

    public NullIdEntity(@org.jspecify.annotations.Nullable String id) {
      this.id = id;
    }

    public @org.jspecify.annotations.Nullable String getId() {
      return id;
    }
  }

  /** Identifier member carrying a second role annotation. */
  @JsonApiResource(type = "dup")
  public static final class DuplicateRoleEntity {
    @JsonApiId
    @JsonApiAttribute
    private final String id;

    public DuplicateRoleEntity(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }
  }

  /** Attribute and relationship mapped onto the same member name. */
  @JsonApiResource(type = "collision")
  public static final class NameCollisionEntity {
    @JsonApiId private final String id;
    @JsonApiAttribute(name = "same") private final String fieldA;
    @JsonApiRelationship(name = "same") private final String fieldB;

    public NameCollisionEntity(String id, String fieldA, String fieldB) {
      this.id = id;
      this.fieldA = fieldA;
      this.fieldB = fieldB;
    }

    public String getId() {
      return id;
    }

    public String getFieldA() {
      return fieldA;
    }

    public String getFieldB() {
      return fieldB;
    }
  }

  /** Two attributes mapped onto the same wire name. */
  @JsonApiResource(type = "dup-attrs")
  public static final class DuplicateAttrNameEntity {
    @JsonApiId private final String id;
    @JsonApiAttribute(name = "same") private final String fieldA;
    @JsonApiAttribute(name = "same") private final String fieldB;

    public DuplicateAttrNameEntity(String id, String fieldA, String fieldB) {
      this.id = id;
      this.fieldA = fieldA;
      this.fieldB = fieldB;
    }

    public String getId() {
      return id;
    }

    public String getFieldA() {
      return fieldA;
    }

    public String getFieldB() {
      return fieldB;
    }
  }

  /** Two relationships mapped onto the same wire name. */
  @JsonApiResource(type = "dup-rels")
  public static final class DuplicateRelNameEntity {
    @JsonApiId private final String id;
    @JsonApiRelationship(name = "same") private final String otherA;
    @JsonApiRelationship(name = "same") private final String otherB;

    public DuplicateRelNameEntity(String id, String otherA, String otherB) {
      this.id = id;
      this.otherA = otherA;
      this.otherB = otherB;
    }

    public String getId() {
      return id;
    }

    public String getOtherA() {
      return otherA;
    }

    public String getOtherB() {
      return otherB;
    }
  }

  /** Attribute override containing characters JSON:API forbids. */
  @JsonApiResource(type = "invalid")
  public static final class InvalidAttrNameEntity {
    @JsonApiId private final String id;
    @JsonApiAttribute(name = "bad name!") private final String value;

    public InvalidAttrNameEntity(String id, String value) {
      this.id = id;
      this.value = value;
    }

    public String getId() {
      return id;
    }

    public String getValue() {
      return value;
    }
  }

  /** Attribute override using the reserved {@code type} member name. */
  @JsonApiResource(type = "reserved-attr")
  public static final class ReservedAttrNameEntity {
    @JsonApiId private final String id;
    @JsonApiAttribute(name = "type") private final String value;

    public ReservedAttrNameEntity(String id, String value) {
      this.id = id;
      this.value = value;
    }

    public String getId() {
      return id;
    }

    public String getValue() {
      return value;
    }
  }

  /** Relationship override containing characters JSON:API forbids. */
  @JsonApiResource(type = "invalid-rel")
  public static final class InvalidRelNameEntity {
    @JsonApiId private final String id;
    @JsonApiRelationship(name = "bad name!") private final String other;

    public InvalidRelNameEntity(String id, String other) {
      this.id = id;
      this.other = other;
    }

    public String getId() {
      return id;
    }

    public String getOther() {
      return other;
    }
  }

  /** Relationship override using the reserved {@code id} member name. */
  @JsonApiResource(type = "reserved-rel")
  public static final class ReservedRelNameEntity {
    @JsonApiId private final String id;
    @JsonApiRelationship(name = "id") private final String other;

    public ReservedRelNameEntity(String id, String other) {
      this.id = id;
      this.other = other;
    }

    public String getId() {
      return id;
    }

    public String getOther() {
      return other;
    }
  }

  /** Attribute getter that always throws. */
  @JsonApiResource(type = "failing-attr")
  public static final class FailingAttrEntity {
    @JsonApiId private final String id;
    @JsonApiAttribute private final String badAttr;

    public FailingAttrEntity(String id, String badAttr) {
      this.id = id;
      this.badAttr = badAttr;
    }

    public String getId() {
      return id;
    }

    public String getBadAttr() throws java.io.IOException {
      throw new java.io.IOException("attribute read failure");
    }
  }

  /** Renamed attribute getter that always throws; failures must report the wire name. */
  @JsonApiResource(type = "renamed-failing-attr")
  public static final class RenamedFailingAttrEntity {
    @JsonApiId private final String id;
    @JsonApiAttribute(name = "body-text") private final String badAttr;

    public RenamedFailingAttrEntity(String id, String badAttr) {
      this.id = id;
      this.badAttr = badAttr;
    }

    public String getId() {
      return id;
    }

    public String getBadAttr() throws java.io.IOException {
      throw new java.io.IOException("attribute read failure");
    }
  }

  /** Identifier getter that always throws. */
  @JsonApiResource(type = "failing-id")
  public static final class FailingIdEntity {
    @JsonApiId private final String id;

    public FailingIdEntity(String id) {
      this.id = id;
    }

    public String getId() throws java.io.IOException {
      throw new java.io.IOException("id read failure");
    }
  }

  /** Annotated property with only a setter and no readable accessor. */
  @JsonApiResource(type = "write-only")
  public static final class MissingAccessorEntity {
    @JsonApiId private final String id;
    private @org.jspecify.annotations.Nullable String secret;

    public MissingAccessorEntity(String id) {
      this.id = id;
    }

    public String getId() {
      return id;
    }

    @JsonApiAttribute
    public void setSecret(@org.jspecify.annotations.Nullable String secret) {
      this.secret = secret;
    }
  }

  /** To-many relationship declared as an unsupported runtime array type. */
  @JsonApiResource(type = "raw-array-rel")
  public static final class RenamedArrayRelEntity {
    @JsonApiId private final String id;
    @JsonApiRelationship(name = "ext-values") private final long[] values;

    public RenamedArrayRelEntity(String id, long[] values) {
      this.id = id;
      this.values = values;
    }

    public String getId() {
      return id;
    }

    public long[] getValues() {
      return values;
    }
  }

  /** To-many relationship holding mixed element types. */
  @JsonApiResource(type = "mixed-rel")
  public static final class RenamedMixedRelEntity {
    @JsonApiId private final String id;
    @JsonApiRelationship(name = "ext-items") private final List<Object> items;

    public RenamedMixedRelEntity(String id, List<Object> items) {
      this.id = id;
      this.items = items;
    }

    public String getId() {
      return id;
    }

    public List<Object> getItems() {
      return items;
    }
  }

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
  public static final class RenamedBagRelEntity {
    @JsonApiId private final String id;
    @JsonApiRelationship(name = "ext-bag") private final RawBag things;

    public RenamedBagRelEntity(String id, RawBag things) {
      this.id = id;
      this.things = things;
    }

    public String getId() {
      return id;
    }

    public RawBag getThings() {
      return things;
    }
  }
}
