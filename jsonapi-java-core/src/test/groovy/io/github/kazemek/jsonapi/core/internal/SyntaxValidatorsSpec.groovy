package io.github.kazemek.jsonapi.core.internal

import spock.lang.Specification

class SyntaxValidatorsSpec extends Specification {

  def "validates uri references"() {
    expect:
    SyntaxValidators.isValidUriReference("http://example.com/path")
    SyntaxValidators.isValidUriReference("/relative/path")
    !SyntaxValidators.isValidUriReference("http://example.com/with spaces")
    !SyntaxValidators.isValidUriReference("")
  }

  def "validates link relations"() {
    expect:
    SyntaxValidators.isValidLinkRelation("self")
    SyntaxValidators.isValidLinkRelation("http://example.com/rel")
    !SyntaxValidators.isValidLinkRelation("_bad")
  }

  def "validates language tags"() {
    expect:
    SyntaxValidators.isValidLanguageTag("en")
    SyntaxValidators.isValidLanguageTag("en-US")
    !SyntaxValidators.isValidLanguageTag("a")
    !SyntaxValidators.isValidLanguageTag("invalid tag!")
  }

  def "validates media types"() {
    expect:
    SyntaxValidators.isValidMediaType("application/vnd.api+json")
    SyntaxValidators.isValidMediaType("text/html")
    SyntaxValidators.isValidMediaType('application/vnd.api+json; profile="https://example.com/profile"')
    SyntaxValidators.isValidMediaType("application/vnd.api+json;charset=utf-8")
    !SyntaxValidators.isValidMediaType("not-a-media-type")
    !SyntaxValidators.isValidMediaType("application/vnd.api+json;")
  }
}
