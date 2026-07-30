package io.github.kazemek.jsonapi.jackson3

import java.lang.reflect.Modifier

import tools.jackson.databind.json.JsonMapper

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode

import spock.lang.Specification

class DocumentWriterIsolationSpec extends Specification {

  static class SampleBean {
    String name

    SampleBean() {}

    SampleBean(String name) {
      this.name = name
    }

    String getName() {
      return name
    }

    void setName(String name) {
      this.name = name
    }
  }

  def "deriving a document writer does not change caller ordinary serialization"() {
    given:
    def caller = JsonMapper.builder().build()
    def before = caller.writeValueAsString(new SampleBean("alpha"))

    when:
    def writer = JsonApiJackson3.writer(caller)
    def after = caller.writeValueAsString(new SampleBean("alpha"))

    then:
    before == after
    before == '{"name":"alpha"}'
    !writer.mapper().is(caller)
  }

  def "deriving a writer from a builder does not register the module on the builder"() {
    given:
    def builder = JsonMapper.builder()
    def before = builder.build().writeValueAsString(new SampleBean("beta"))

    when:
    def writer = JsonApiJackson3.writer(builder)
    def after = builder.build().writeValueAsString(new SampleBean("beta"))

    then:
    before == after
    before == '{"name":"beta"}'
    !writer.mapper().is(builder.build())
  }

  def "documentMapper is package-private and not a public factory method"() {
    expect:
    def method = JsonApiJackson3.getDeclaredMethod('documentMapper', JsonMapper)
    !Modifier.isPublic(method.modifiers)
    JsonApiJackson3.methods.every { it.name != 'documentMapper' }
  }

  def "JsonApiDocumentWriter.mapper is package-private"() {
    expect:
    def method = JsonApiDocumentWriter.getDeclaredMethod('mapper')
    !Modifier.isPublic(method.modifiers)
    JsonApiDocumentWriter.methods.every { it.name != 'mapper' }
  }

  def "aggregate-invalid documents cannot be written through public writer APIs"() {
    given:
    def writer = JsonApiJackson3.writer(JsonMapper.builder().build())
    def invalid = new JsonApiDocument(
        new DocumentData.ResourceCollection([
          ResourceObject.of('articles', '1'),
          ResourceObject.of('articles', '1'),
        ]),
        null,
        null,
        null,
        null,
        null,
        [:])

    when:
    writer.writeValueAsString(invalid)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.DUPLICATE_RESOURCE_IDENTITY
  }
}
