package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.annotation.JsonApiAttribute
import io.github.kazemek.jsonapi.annotation.JsonApiId
import io.github.kazemek.jsonapi.annotation.JsonApiResource
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson.StructuredMember
import io.github.kazemek.jsonapi.jackson.StructuredMemberState
import io.github.kazemek.jsonapi.jackson.StructuredPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.SnakeAddress
import io.github.kazemek.jsonapi.jackson3.testmodel.SnakeAddressPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.ThrowingAddressPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.WrapperCustomizedAddressPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.AddressPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithAddressPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithOptionalAddress
import spock.lang.Specification
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper

class PatchStructuredBindingSpec extends Specification {

  def "naming strategy applies to top-level and nested structured marker maps"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"street_name":"S","city":"C"}}}}'

    when:
    def dto = reader.readValue(json, SnakeAddressPatchDto)

    then:
    dto.id == "1"
    dto.address == PatchPresence.present(
        new SnakeAddressPatch(PatchPresence.present("S"), PatchPresence.present("C")))
  }

  def "naming strategy nested unknown member reports the wire pointer"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"street_name":"S","bogus":"x"}}}}'

    when:
    reader.readValue(json, SnakeAddressPatchDto)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.UNKNOWN_PATCH_MEMBER
    ex.propertyPath() == "/attributes/address/bogus"
  }

  def "low-level naming strategy carries both wire and logical nested names"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchReader(mapper)
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"street_name":"S"}}}}'

    when:
    def command = reader.readValue(json, SnakeAddressArticle)

    then:
    command.changes() == [
      new PatchChange.AttributeChange(
      "address", "address",
      new StructuredPatch([
        new StructuredMember("street_name", "streetName", new StructuredMemberState.Atomic("S"))
      ]))
    ]
  }

  def "wrapper-level @JsonDeserialize on a nested presence-aware member is rejected lazily"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"street":"S","city":"C"}}}}'

    when:
    reader.readValue(json, WrapperCustomizedAddressPatchDto)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.INVALID_PATCH_PROPERTY_TYPE
    ex.propertyPath() == "/attributes/address/city"
  }

  def "deep shape-translated construction failure reports the wire pointer"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"street":"S","geo":{"lat":"1"}}}}}'

    when:
    reader.readValue(json, ThrowingGeoPatchDto)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
    ex.propertyPath() == "/attributes/address/geo"
  }

  def "deep construction failure path is translated under a naming strategy"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"street":"S","geo":{"lat":"1"}}}}}'

    when:
    reader.readValue(json, ThrowingGeoPatchDto)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
    ex.propertyPath() == "/attributes/address/geo"
  }

  def "naming strategy accepts the logical member name for nested marker maps"() {
    given:
    def mapper = JsonMapper.builder()
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .build()
    def reader = JsonApiJackson3.patchDtoReader(mapper)
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"streetName":"S","city":"C"}}}}'

    when:
    def dto = reader.readValue(json, SnakeAddressPatchDto)

    then:
    dto.address == PatchPresence.present(
        new SnakeAddressPatch(PatchPresence.present("S"), PatchPresence.present("C")))
  }

  def "nested property-level @JsonDeserialize member stays atomic on the low-level path"() {
    given:
    def reader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def json =
        '{"data":{"type":"articles","id":"1","attributes":{"address":{"street":"S","note":"n"}}}}'

    when:
    def command = reader.readValue(json, io.github.kazemek.jsonapi.jackson3.testmodel.AddressWithLoudNoteArticle)

    then:
    command.changes() == [
      new PatchChange.AttributeChange(
      "address", "address",
      new StructuredPatch([
        new StructuredMember("note", "note", new StructuredMemberState.Atomic("n")),
        new StructuredMember("street", "street", new StructuredMemberState.Atomic("S"))
      ]))
    ]
  }

  def "top-level construction failure with an empty path reports the root pointer"() {
    given:
    def reader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    reader.readValue(json, io.github.kazemek.jsonapi.jackson3.testmodel.ThrowingArticlePatch)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.MISSING_CREATOR_INPUT
    ex.propertyPath() == "/"
  }

  def "typed and low-level paths express the same nested presence for the same request"() {
    given:
    def typedReader = JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
    def lowLevelReader = JsonApiJackson3.patchReader(JsonMapper.builder().build())
    def partial = '{"data":{"type":"articles","id":"1","attributes":{"address":{"street":"New Street"}}}}'
    def withNull = '{"data":{"type":"articles","id":"1","attributes":{"address":{"street":"S","city":null}}}}'
    def empty = '{"data":{"type":"articles","id":"1","attributes":{"address":{}}}}'

    when:
    def typedPartial = typedReader.readValue(partial, ArticleWithAddressPatch)
    def lowPartial = lowLevelReader.readValue(partial, ArticleWithOptionalAddress)
    def typedNull = typedReader.readValue(withNull, ArticleWithAddressPatch)
    def lowNull = lowLevelReader.readValue(withNull, ArticleWithOptionalAddress)
    def typedEmpty = typedReader.readValue(empty, ArticleWithAddressPatch)
    def lowEmpty = lowLevelReader.readValue(empty, ArticleWithOptionalAddress)

    then: // nested Present(value) <-> Atomic(value); nested Omitted <-> absent member
    ((PatchPresence.Present) typedPartial.address()).value() ==
        new AddressPatch(PatchPresence.present("New Street"), PatchPresence.omitted())
    ((StructuredPatch) lowPartial.changes()[0].value()).members() == [
      new StructuredMember("street", "street", new StructuredMemberState.Atomic("New Street"))
    ]
    then: // nested Present(null) <-> Atomic(null)
    ((PatchPresence.Present) typedNull.address()).value() ==
        new AddressPatch(PatchPresence.present("S"), PatchPresence.present(null))
    ((StructuredPatch) lowNull.changes()[0].value()).members() == [
      new StructuredMember("street", "street", new StructuredMemberState.Atomic("S")),
      new StructuredMember("city", "city", new StructuredMemberState.Atomic(null))
    ]
    then: // Present(empty object) <-> empty StructuredPatch
    ((PatchPresence.Present) typedEmpty.address()).value() ==
        new AddressPatch(PatchPresence.omitted(), PatchPresence.omitted())
    ((StructuredPatch) lowEmpty.changes()[0].value()).members().isEmpty()
  }

  @JsonApiResource(type = "articles")
  static class SnakeAddressPatchDto {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<SnakeAddressPatch> address
  }

  @JsonApiResource(type = "articles")
  static class SnakeAddressArticle {
    @JsonApiId String id
    @JsonApiAttribute SnakeAddress address
  }

  @JsonApiResource(type = "articles")
  static class WrapperCustomizedAddressPatchDto {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<WrapperCustomizedAddressPatch> address
  }

  @JsonApiResource(type = "articles")
  static class ThrowingGeoPatchDto {
    @JsonApiId String id
    @JsonApiAttribute PatchPresence<ThrowingAddressPatch> address
  }
}
