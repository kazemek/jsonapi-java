package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class Jackson3JsonApiRelationshipsSpec extends Specification {

  @Shared
  Jackson3JsonApi jsonApi = JsonApiJackson3.jsonApi(JsonMapper.builder().build())

  def "round-trips a to-one linkage document"() {
    given:
    def identifier = ResourceIdentifier.of("people", "p1")

    when:
    def json = jsonApi.relationships().writeToOne(identifier)

    then:
    jsonApi.relationships().readToOne(json) == identifier
  }

  def "explicit null to-one linkage round-trips as null"() {
    when:
    def json = jsonApi.relationships().writeToOne(null)

    then:
    json.contains('"data":null')
    jsonApi.relationships().readToOne(json) == null
  }

  def "round-trips a to-many linkage document including the empty collection"() {
    given:
    def identifiers = [
      ResourceIdentifier.of("comments", "c1"),
      ResourceIdentifier.of("comments", "c2")
    ]

    when:
    def json = jsonApi.relationships().writeToMany(identifiers)
    def emptyJson = jsonApi.relationships().writeToMany(List.of())

    then:
    jsonApi.relationships().readToMany(json) == identifiers
    jsonApi.relationships().readToMany(emptyJson) == []
  }

  def "to-one reads never accept collections"() {
    given:
    def json = jsonApi.relationships().writeToMany([
      ResourceIdentifier.of("comments", "c1")
    ])

    when:
    jsonApi.relationships().readToOne(json)

    then:
    def ex = thrown(JsonApiMappingException)
    ex.diagnostic() == MappingDiagnostic.RESOURCE_TYPE_MISMATCH
    ex.propertyPath() == "/data"
  }

  def "to-many reads never accept one identifier or null"() {
    given:
    def single = jsonApi.relationships().writeToOne(ResourceIdentifier.of("people", "p1"))
    def explicitNull = jsonApi.relationships().writeToOne(null)

    when:
    jsonApi.relationships().readToMany(single)

    then:
    thrown(JsonApiMappingException)

    when:
    jsonApi.relationships().readToMany(explicitNull)

    then:
    thrown(JsonApiMappingException)
  }

  def "stream sinks mirror string results without closing caller streams"() {
    given:
    def identifier = ResourceIdentifier.of("people", "p1")
    def out = new ByteArrayOutputStream()

    when:
    jsonApi.relationships().writeToOne(identifier, out)

    then:
    jsonApi.relationships().readToOne(new ByteArrayInputStream(out.toByteArray())) == identifier
  }
}
