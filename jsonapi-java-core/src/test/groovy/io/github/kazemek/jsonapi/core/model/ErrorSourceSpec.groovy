package io.github.kazemek.jsonapi.core.model

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class ErrorSourceSpec extends Specification {

  def "null pointer remains valid"() {
    when:
    def source = new ErrorSource(null, "include", null, [:])

    then:
    source.pointer() == null
    source.parameter() == "include"
  }

  def "valid pointer '#pointer' is accepted"() {
    when:
    def source = new ErrorSource(pointer, null, null, [:])

    then:
    source.pointer() == pointer

    where:
    pointer << [
      "",
      "/",
      "/data",
      "/data/0/id",
      "/a~0b",
      "/a~1b",
      "/a~01b",
      "/données"
    ]
  }

  def "invalid pointer '#pointer' fails with INVALID_JSON_POINTER"() {
    when:
    new ErrorSource(pointer, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_JSON_POINTER
    ex.jsonPointer() == "/errors/source/pointer"

    where:
    pointer << [
      "data",
      "/a~",
      "/a~2",
      "/a~x",
      "#/data"
    ]
  }
}
