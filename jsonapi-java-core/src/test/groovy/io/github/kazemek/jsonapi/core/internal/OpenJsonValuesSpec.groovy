package io.github.kazemek.jsonapi.core.internal

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class OpenJsonValuesSpec extends Specification {

  def "rejects non-finite numbers with stable code"() {
    when:
    OpenJsonValues.copy(Double.POSITIVE_INFINITY, "/meta/n")

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_OPEN_JSON_VALUE
    ex.jsonPointer() == "/meta/n"
  }

  def "rejects unsupported types with stable code"() {
    when:
    OpenJsonValues.copy(new Object(), "/meta/x")

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_OPEN_JSON_VALUE
  }

  def "deep copies nested maps and lists"() {
    given:
    def nested = [items: ["a"]]

    when:
    def copy = OpenJsonValues.copy(nested, "/meta") as Map
    nested.items << "b"

    then:
    (copy.items as List).size() == 1
  }

  def "preserves explicit null map values and list elements"() {
    when:
    def mapCopy = OpenJsonValues.copyMap([a: null, b: 1], "/meta")
    def listCopy = OpenJsonValues.copy([null, "x"], "/meta") as List

    then:
    mapCopy.containsKey("a")
    mapCopy.a == null
    mapCopy.b == 1
    listCopy[0] == null
    listCopy[1] == "x"
  }

  def "preserves encounter order for open maps"() {
    when:
    def copy = OpenJsonValues.copyMap([z: 1, a: null, m: 2], "/meta")

    then:
    copy.keySet().toList() == ["z", "a", "m"]
  }
}
