package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson3.testmodel.Article
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class ResourceMapperIsolationSpec extends Specification {

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

  def "deriving a resource mapper does not change caller ordinary serialization"() {
    given:
    def caller = JsonMapper.builder().build()
    def before = caller.writeValueAsString(new SampleBean("alpha"))

    when:
    JsonApiJackson3.resourceMapper(caller)
    def after = caller.writeValueAsString(new SampleBean("alpha"))

    then:
    before == after
    before == '{"name":"alpha"}'
  }

  def "deriving a resource mapper from a builder does not mutate the builder"() {
    given:
    def builder = JsonMapper.builder()
    def before = builder.build().writeValueAsString(new SampleBean("beta"))

    when:
    JsonApiJackson3.resourceMapper(builder)
    def after = builder.build().writeValueAsString(new SampleBean("beta"))

    then:
    before == after
    before == '{"name":"beta"}'
  }

  def "resource mapper with IdentifierConverter overload works"() {
    given:
    def jsonMapper = JsonMapper.builder().build()
    def mapper = JsonApiJackson3.resourceMapper(jsonMapper, IdentifierConverter.defaults())

    when:
    def article = new Article("1", "T", "B", [], null)
    def resource = mapper.toResource(article)

    then:
    resource.id() == "1"
  }
}
