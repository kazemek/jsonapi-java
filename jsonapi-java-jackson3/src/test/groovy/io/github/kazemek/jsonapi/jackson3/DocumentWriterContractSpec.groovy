package io.github.kazemek.jsonapi.jackson3

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.testfixtures.FixtureDirectory
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios

import spock.lang.Shared
import spock.lang.Specification

class DocumentWriterContractSpec extends Specification {

  @Shared
  Path fixturesDir = FixtureDirectory.jsonApiFixtures()

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
    fixture << CodecScenarios.writable()
  }

  def "emits exact UTF-8 for #fixture.id"() {
    given:
    def writer = JsonApiJackson3.writer(mapper, fixture.context)
    def expected = readFixtureText(fixture.exactUtf8Path).trim()

    expect:
    writer.writeValueAsString(fixture.document) == expected

    where:
    fixture << CodecScenarios.exactUtf8()
  }

  def "emits array-form hreflang for #fixture.id"() {
    given:
    def writer = JsonApiJackson3.writer(mapper, fixture.context)
    def json = writer.writeValueAsString(fixture.document)

    expect:
    json.contains('"hreflang":["en"]')
    !json.contains('"hreflang":"en"')

    where:
    fixture << CodecScenarios.hreflangArray()
  }

  private JsonNode readFixtureJson(String relativePath) {
    return mapper.readTree(readFixtureText(relativePath))
  }

  private String readFixtureText(String relativePath) {
    return Files.readString(fixturesDir.resolve(relativePath), StandardCharsets.UTF_8)
  }
}
