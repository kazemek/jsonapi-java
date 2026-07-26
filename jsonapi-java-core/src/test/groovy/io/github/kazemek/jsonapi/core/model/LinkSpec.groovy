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
    new Link.ObjectLink("http://example.com", null, null, null, null, ["invalid tag!"], null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_LANGUAGE_TAG
  }

  def "invalid link relation fails"() {
    when:
    new Link.ObjectLink("http://example.com", "_bad", null, null, null, null, null, [:])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_LINK_RELATION
  }

  def "invalid media type fails"() {
    when:
    new Link.ObjectLink("http://example.com", null, null, null, "not-a-type", null, null, [:])

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

  def "object link preserves extension and at additional members"() {
    when:
    def link = new Link.ObjectLink(
        "http://example.com", null, null, null, null, null, null,
        ["ext:flag": true, "@context": "https://example.com/ctx"])

    then:
    link.additionalMembers()["ext:flag"] == true
    link.additionalMembers()["@context"] == "https://example.com/ctx"
  }

  def "object link rejects reserved additional member names"() {
    when:
    new Link.ObjectLink("http://example.com", null, null, null, null, null, null, [href: "x"])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }

  def "links input map is defensively copied"() {
    given:
    def input = [self: new Link.StringLink("http://example.com")]
    def links = Links.ofLinks(input)

    when:
    input.next = new Link.StringLink("http://example.com/2")

    then:
    !links.links().containsKey("next")
  }

  def "at members are rejected as semantic link keys"() {
    when:
    Links.ofLinks(["@context": new Link.StringLink("http://example.com")])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.RESERVED_FIELD_NAME
  }

  def "invalid link relation name is rejected at construction"() {
    when:
    Links.ofLinks(["has_underscore": new Link.StringLink("http://example.com")])

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.INVALID_LINK_RELATION
  }

  def "extension-shaped link keys are accepted locally"() {
    when:
    def links = Links.ofLinks(["ext:custom": new Link.StringLink("http://example.com")])

    then:
    links.links().containsKey("ext:custom")
  }

  def "at members are accepted via additional members"() {
    when:
    def links = Links.of([:], ["@context": "https://example.com/ctx"])

    then:
    links.additionalMembers()["@context"] == "https://example.com/ctx"
    links.flatten().containsKey("@context")
  }
}
