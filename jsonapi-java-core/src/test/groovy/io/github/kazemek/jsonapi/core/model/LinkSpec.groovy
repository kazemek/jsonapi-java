package io.github.kazemek.jsonapi.core.model

import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import spock.lang.Specification

class LinkSpec extends Specification {

  def "hreflang canonical list representation accepts single language"() {
    when:
    def link = Link.ObjectLink.withHreflang("http://example.com", "en")

    then:
    link.hreflang() == ["en"]
  }

  def "hreflang accepts multiple languages"() {
    when:
    def link = Link.ObjectLink.withHreflang("http://example.com", ["en", "de"])

    then:
    link.hreflang() == ["en", "de"]
  }

  def "invalid href fails with stable rule code"() {
    when:
    new Link.StringLink("not a valid uri with spaces")

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_URI_REFERENCE
    ex.jsonPointer().contains("href")
  }

  def "invalid language tag fails"() {
    when:
    new Link.ObjectLink("http://example.com", null, null, null, null, ["invalid tag!"], null)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_LANGUAGE_TAG
  }

  def "invalid link relation fails"() {
    when:
    new Link.ObjectLink("http://example.com", "_bad", null, null, null, null, null)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_LINK_RELATION
  }

  def "invalid media type fails"() {
    when:
    new Link.ObjectLink("http://example.com", null, null, null, "not-a-type", null, null)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_MEDIA_TYPE
  }

  def "nullable pagination links are preserved"() {
    when:
    def links = Links.ofLinks([
      first: new Link.StringLink("http://example.com/1"),
      next : null
    ])

    then:
    links.links().containsKey("next")
    links.links().get("next") == null
  }
}
