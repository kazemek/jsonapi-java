package io.github.kazemek.jsonapi.jackson3

import java.nio.charset.StandardCharsets

import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixtures

import spock.lang.Specification

class DocumentWriterSinkSpec extends Specification {

  def "all write sinks emit equivalent JSON and expose bound context"() {
    given:
    def mapper = JsonMapper.builder().build()
    def fixture = WriterFixtures.byId('single-resource')
    def writer = JsonApiJackson3.writer(mapper, fixture.context)
    def expected = mapper.readTree(writer.writeValueAsString(fixture.document))

    def bytesOut = new ByteArrayOutputStream()
    def charsOut = new StringWriter()
    def generatorOut = new ByteArrayOutputStream()

    when:
    writer.writeValue(bytesOut, fixture.document)
    writer.writeValue(charsOut, fixture.document)
    def generator = writer.mapper().createGenerator(generatorOut)
    try {
      writer.writeValue(generator, fixture.document)
    } finally {
      generator.close()
    }

    then:
    writer.context().is(fixture.context)
    mapper.readTree(writer.writeValueAsBytes(fixture.document)) == expected
    mapper.readTree(bytesOut.toByteArray()) == expected
    mapper.readTree(charsOut.toString()) == expected
    mapper.readTree(generatorOut.toByteArray()) == expected
    new String(bytesOut.toByteArray(), StandardCharsets.UTF_8) == charsOut.toString()
  }
}
