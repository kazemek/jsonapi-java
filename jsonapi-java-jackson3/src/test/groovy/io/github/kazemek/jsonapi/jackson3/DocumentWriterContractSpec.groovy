package io.github.kazemek.jsonapi.jackson3

import java.nio.charset.StandardCharsets

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import io.github.kazemek.jsonapi.testsupport.codec.CodecScenarios

import spock.lang.Shared
import spock.lang.Specification

class DocumentWriterContractSpec extends Specification {

  @Shared
  JsonMapper mapper = JsonMapper.builder().build()

  def "writes fixture #fixture.id"() {
    given:
    def expected = readFixtureJson(fixture.expectedPath)
    def writer = JsonApiJackson3.writer(mapper, fixture.context)

    when:
    def asString = writer.writeValueAsString(fixture.document)
    def asBytes = writer.writeValueAsBytes(fixture.document)

    then:
    mapper.readTree(asString) == expected
    mapper.readTree(asBytes) == expected
    new String(asBytes, StandardCharsets.UTF_8) == asString

    where:
    fixture << CodecScenarios.catalog().where { it.writable }
  }

  def "emits exact UTF-8 for #fixture.id"() {
    given:
    def writer = JsonApiJackson3.writer(mapper, fixture.context)
    def expected = readFixtureText(fixture.exactUtf8Path).trim()

    expect:
    writer.writeValueAsString(fixture.document) == expected

    where:
    fixture << CodecScenarios.catalog().where { it.assertExactUtf8 }
  }

  def "emits array-form hreflang for #fixture.id"() {
    given:
    def writer = JsonApiJackson3.writer(mapper, fixture.context)
    def json = writer.writeValueAsString(fixture.document)

    expect:
    json.contains('"hreflang":["en"]')
    !json.contains('"hreflang":"en"')

    where:
    fixture << CodecScenarios.catalog().where { it.assertHreflangArray }
  }

  private JsonNode readFixtureJson(String relativePath) {
    return mapper.readTree(readFixtureBytes(relativePath))
  }

  private static String readFixtureText(String relativePath) {
    return TestSupportResources.readCorpusUtf8(relativePath)
  }

  private static byte[] readFixtureBytes(String relativePath) {
    return TestSupportResources.readCorpusBytes(relativePath)
  }
}
