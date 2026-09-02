package io.github.kazemek.jsonapi.jackson3

import java.nio.charset.StandardCharsets

import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.ResourceIdentity
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.fixtures.TestFixtureResources
import io.github.kazemek.jsonapi.jackson.mapping.MappedDocument

import spock.lang.Specification

class DocumentWriterSinkSpec extends Specification {

  def "all write sinks emit equivalent JSON and expose bound context"() {
    given:
    def mapper = JsonMapper.builder().build()
    def context = ValidationContext.defaults()
    def resource = new ResourceObject(
        'articles',
        '1',
        null,
        Attributes.ofAttributes(['title': 'JSON:API paints my bikeshed!']),
        null,
        null,
        null,
        [:])
    def document = JsonApiDocument.withData(new DocumentData.SingleResource(resource))
    def writer = JsonApiJackson3.writer(mapper, context)
    def expected = mapper.readTree(
        TestFixtureResources.readCorpusUtf8('documents/single-resource.json'))

    def bytesOut = new ByteArrayOutputStream()
    def charsOut = new StringWriter()
    def generatorOut = new ByteArrayOutputStream()

    when:
    writer.writeValue(bytesOut, document)
    writer.writeValue(charsOut, document)
    def generator = writer.mapper().createGenerator(generatorOut)
    try {
      writer.writeValue(generator, document)
    } finally {
      generator.close()
    }

    then:
    writer.context().is(context)
    mapper.readTree(writer.writeValueAsString(document)) == expected
    mapper.readTree(writer.writeValueAsBytes(document)) == expected
    mapper.readTree(bytesOut.toByteArray()) == expected
    mapper.readTree(charsOut.toString()) == expected
    mapper.readTree(generatorOut.toByteArray()) == expected
    new String(bytesOut.toByteArray(), StandardCharsets.UTF_8) == charsOut.toString()
  }

  def "mapped documents use every write sink"() {
    given:
    def mapper = JsonMapper.builder().build()
    def resource = new ResourceObject(
        'articles',
        '1',
        null,
        Attributes.ofAttributes(['title': 'JSON:API paints my bikeshed!']),
        null,
        null,
        null,
        [:])
    def document = JsonApiDocument.withData(new DocumentData.SingleResource(resource))
    def mapped = new MappedDocument(document, Set.of(ResourceIdentity.ofId('people', 'p1')))
    def plainMapped = new MappedDocument(document, Set.of())
    def writer = JsonApiJackson3.writer(mapper, ValidationContext.defaults())
    def bytesOut = new ByteArrayOutputStream()
    def charsOut = new StringWriter()
    def generatorOut = new ByteArrayOutputStream()

    when:
    def stringValue = writer.writeValueAsString(mapped)
    def byteValue = writer.writeValueAsBytes(mapped)
    writer.writeValue(bytesOut, mapped)
    writer.writeValue(charsOut, mapped)
    def generator = writer.mapper().createGenerator(generatorOut)
    try {
      writer.writeValue(generator, mapped)
    } finally {
      generator.close()
    }

    then:
    mapper.readTree(stringValue) == mapper.readTree(byteValue)
    mapper.readTree(stringValue) == mapper.readTree(bytesOut.toByteArray())
    mapper.readTree(stringValue) == mapper.readTree(charsOut.toString())
    mapper.readTree(stringValue) == mapper.readTree(generatorOut.toByteArray())
    writer.writeValueAsString(plainMapped) == stringValue
  }
}
