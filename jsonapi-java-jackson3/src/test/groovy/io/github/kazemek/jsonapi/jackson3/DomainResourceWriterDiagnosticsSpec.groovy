package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiRelationship
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class DomainResourceWriterDiagnosticsSpec extends Specification {

  def "missing @JsonApiResource throws MISSING_RESOURCE_ANNOTATION"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

    when:
    mapper.toResource(new Object())

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_RESOURCE_ANNOTATION
  }

  @JsonApiResource(type = "")
  static class EmptyTypeEntity {
    @JsonApiId String id
  }

  def "empty resource type throws INVALID_RESOURCE_TYPE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new EmptyTypeEntity(id: "1")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_RESOURCE_TYPE
  }

  @JsonApiResource(type = "bad type!")
  static class InvalidTypeEntity {
    @JsonApiId String id
  }

  def "invalid resource type name throws INVALID_RESOURCE_TYPE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new InvalidTypeEntity(id: "1")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_RESOURCE_TYPE
  }

  @JsonApiResource(type = "entities")
  static class NoIdEntity {
    String name
  }

  def "missing identifier throws MISSING_IDENTIFIER"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new NoIdEntity(name: "test")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_IDENTIFIER
  }

  @JsonApiResource(type = "entities")
  static class NullIdEntity {
    @JsonApiId String id
  }

  def "null identifier throws MISSING_IDENTIFIER"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new NullIdEntity(id: null)

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_IDENTIFIER
    // The wire identifier coordinate, not the Jackson logical property identity.
    ex.propertyPath() == "/id"
  }

  @JsonApiResource(type = "dup")
  static class DuplicateRoleEntity {
    @JsonApiId
    @JsonApiAttribute
    String id
  }

  def "duplicate role annotations throw DUPLICATE_ROLE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new DuplicateRoleEntity(id: "1")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.DUPLICATE_ROLE
  }

  @JsonApiResource(type = "collision")
  static class NameCollisionEntity {
    @JsonApiId String id
    @JsonApiAttribute(name = "same") String fieldA
    @JsonApiRelationship(name = "same") String fieldB
  }

  def "attribute/relationship name collision throws NAME_COLLISION"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new NameCollisionEntity(id: "1", fieldA: "a", fieldB: "b")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NAME_COLLISION
  }

  @JsonApiResource(type = "invalid")
  static class InvalidAttrNameEntity {
    @JsonApiId String id
    @JsonApiAttribute(name = "bad name!") String value
  }

  def "invalid attribute name override throws INVALID_ATTRIBUTE_NAME"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new InvalidAttrNameEntity(id: "1", value: "v")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_ATTRIBUTE_NAME
  }

  @JsonApiResource(type = "invalid-rel")
  static class InvalidRelNameEntity {
    @JsonApiId String id
    @JsonApiRelationship(name = "bad name!") String other
  }

  def "invalid relationship name override throws INVALID_RELATIONSHIP_NAME"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new InvalidRelNameEntity(id: "1", other: "o")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_RELATIONSHIP_NAME
  }

  @JsonApiResource(type = "failing-attr")
  static class FailingAttrEntity {
    @JsonApiId String id
    @JsonApiAttribute String badAttr

    String getBadAttr() throws IOException {
      throw new IOException("attribute read failure")
    }
  }

  def "attribute getter failure throws UNSUPPORTED_ATTRIBUTE_VALUE"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new FailingAttrEntity(id: "1", badAttr: "anything")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    ex.propertyPath() == "/attributes/badAttr"
  }

  @JsonApiResource(type = "renamed-failing-attr")
  static class RenamedFailingAttrEntity {
    @JsonApiId String id
    @JsonApiAttribute(name = "body-text") String badAttr

    String getBadAttr() throws IOException {
      throw new IOException("attribute read failure")
    }
  }

  def "renamed attribute getter failure reports the wire name, never the logical name"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new RenamedFailingAttrEntity(id: "1", badAttr: "anything")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    // Wire member body-text; the Jackson/logical name badAttr must not leak into the location.
    ex.propertyPath() == "/attributes/body-text"
  }

  @JsonApiResource(type = "failing-id")
  static class FailingIdEntity {
    @JsonApiId String id

    String getId() throws IOException {
      throw new IOException("id read failure")
    }
  }

  def "identifier getter failure throws MISSING_IDENTIFIER"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new FailingIdEntity(id: "1")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_IDENTIFIER
    ex.propertyPath() == "/id"
  }

  @JsonApiResource(type = "dup-attrs")
  static class DuplicateAttrNameEntity {
    @JsonApiId String id
    @JsonApiAttribute(name = "same") String fieldA
    @JsonApiAttribute(name = "same") String fieldB
  }

  def "duplicate attribute names throw NAME_COLLISION"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new DuplicateAttrNameEntity(id: "1", fieldA: "a", fieldB: "b")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NAME_COLLISION
    // The duplicated attribute container coordinate.
    ex.propertyPath() == "/attributes/same"
  }

  @JsonApiResource(type = "dup-rels")
  static class DuplicateRelNameEntity {
    @JsonApiId String id
    @JsonApiRelationship(name = "same") String otherA
    @JsonApiRelationship(name = "same") String otherB
  }

  def "duplicate relationship names throw NAME_COLLISION"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new DuplicateRelNameEntity(id: "1", otherA: "a", otherB: "b")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.NAME_COLLISION
    ex.propertyPath() == "/relationships/same/data"
  }

  @JsonApiResource(type = "reserved-attr")
  static class ReservedAttrNameEntity {
    @JsonApiId String id
    @JsonApiAttribute(name = "type") String value
  }

  def "reserved attribute name type throws INVALID_ATTRIBUTE_NAME"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new ReservedAttrNameEntity(id: "1", value: "v")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_ATTRIBUTE_NAME
  }

  @JsonApiResource(type = "reserved-rel")
  static class ReservedRelNameEntity {
    @JsonApiId String id
    @JsonApiRelationship(name = "id") String other
  }

  def "reserved relationship name id throws INVALID_RELATIONSHIP_NAME"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new ReservedRelNameEntity(id: "1", other: "o")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_RELATIONSHIP_NAME
  }

  @JsonApiResource(type = "write-only")
  static class MissingAccessorEntity {
    @JsonApiId String id
    private String secret

    @JsonApiAttribute
    void setSecret(String secret) {
      this.secret = secret
    }
  }

  def "annotated property without readable accessor throws MISSING_ACCESSOR"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def entity = new MissingAccessorEntity(id: "1")
    entity.setSecret("hidden")

    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_ACCESSOR
    ex.propertyPath() == "/attributes/secret"
  }

  // ============================== NO-LOCATION DIAGNOSTICS ==============================
  //
  // Class-level and specification failures have no document member coordinate; the identifying
  // names stay in the message and the location is absent (never "", "/", or a logical name).

  def "class-level declaration failures carry no location"() {
    when:
    mapper.toResource(entity)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == diagnostic
    ex.location() == null

    where:
    diagnostic                              | entity
    MappingDiagnostic.MISSING_RESOURCE_ANNOTATION | new Object()
    MappingDiagnostic.INVALID_RESOURCE_TYPE  | new EmptyTypeEntity(id: "1")
    MappingDiagnostic.INVALID_RESOURCE_TYPE  | new InvalidTypeEntity(id: "1")
    MappingDiagnostic.MISSING_IDENTIFIER     | new NoIdEntity(name: "test")
    MappingDiagnostic.DUPLICATE_ROLE         | new DuplicateRoleEntity(id: "1")
    MappingDiagnostic.NAME_COLLISION         | new NameCollisionEntity(id: "1", fieldA: "a", fieldB: "b")
    MappingDiagnostic.INVALID_ATTRIBUTE_NAME | new InvalidAttrNameEntity(id: "1", value: "v")
    MappingDiagnostic.INVALID_RELATIONSHIP_NAME | new InvalidRelNameEntity(id: "1", other: "o")
    mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
  }
}
