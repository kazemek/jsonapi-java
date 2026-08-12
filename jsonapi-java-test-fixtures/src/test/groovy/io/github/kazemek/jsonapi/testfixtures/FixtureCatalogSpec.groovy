package io.github.kazemek.jsonapi.testfixtures

import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios
import spock.lang.Specification

class FixtureCatalogSpec extends Specification {

  def "construction rejects duplicate scenario ids"() {
    given:
    def scenario = CodecScenarios.all().first()

    when:
    FixtureCatalog.of("codec", [scenario, scenario])

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Duplicate codec scenario id: ${scenario.id}"
  }
}
