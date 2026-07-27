package io.github.kazemek.jsonapi.core

import spock.lang.Specification

class SetupSpec extends Specification {
  def "pipeline works"() {
    given:
    def value = 42

    when:
    def result = value + 1

    then:
    result == 43
  }
}
