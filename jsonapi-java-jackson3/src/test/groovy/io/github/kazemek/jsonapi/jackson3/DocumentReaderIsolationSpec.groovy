package io.github.kazemek.jsonapi.jackson3

import java.lang.reflect.Modifier

import tools.jackson.databind.json.JsonMapper

import spock.lang.Specification

class DocumentReaderIsolationSpec extends Specification {

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

  def "deriving a document reader does not change caller ordinary serialization"() {
    given:
    def caller = JsonMapper.builder().build()
    def before = caller.writeValueAsString(new SampleBean('alpha'))

    when:
    def reader = JsonApiJackson3.reader(caller, DocumentReadContext.resourceDefaults())
    def after = caller.writeValueAsString(new SampleBean('alpha'))

    then:
    before == after
    before == '{"name":"alpha"}'
    !reader.mapper().is(caller)
  }

  def "deriving a reader from a builder does not register the module on the builder"() {
    given:
    def builder = JsonMapper.builder()
    def before = builder.build().writeValueAsString(new SampleBean('beta'))

    when:
    def reader = JsonApiJackson3.reader(builder, DocumentReadContext.resourceDefaults())
    def after = builder.build().writeValueAsString(new SampleBean('beta'))

    then:
    before == after
    before == '{"name":"beta"}'
    !reader.mapper().is(builder.build())
  }

  def "JsonApiDocumentReader.mapper is package-private"() {
    expect:
    def method = JsonApiDocumentReader.getDeclaredMethod('mapper')
    !Modifier.isPublic(method.modifiers)
    JsonApiDocumentReader.methods.every { it.name != 'mapper' }
  }
}
