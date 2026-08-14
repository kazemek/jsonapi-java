package io.github.kazemek.jsonapi.testfixtures.enveloperead

import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import spock.lang.Specification

// Why this spec exists: catalog integrity only inspects scenario structure. Compact constructors
// such as FlatThrowingArticle's boom rejection are not executed there, and Jackson adapter suites
// do not contribute JaCoCo coverage to this module. These tests pin that local surface so new-code
// coverage on the shared envelope-read models stays above the Quality Gate.
class EnvelopeReadFixtureModelsSpec extends Specification {

  def "node DTO preserves a null parent relationship"() {
    expect:
    new FlatNode("1", null).parent() == null
    new FlatNode("1", ResourceIdentifier.of("nodes", "2")).parent() ==
        ResourceIdentifier.of("nodes", "2")
  }

  def "strict article stores a numeric title"() {
    expect:
    new FlatStrictArticle("1", 7).title() == 7
  }

  def "throwing article accepts a non-rejected title"() {
    expect:
    new FlatThrowingArticle("1", "ok").title() == "ok"
    new FlatThrowingArticle("1", null).title() == null
  }

  def "throwing article rejects the boom title"() {
    when:
    new FlatThrowingArticle("1", "boom")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "creator rejected value"
  }

  def "registry-rejection records are constructible"() {
    expect:
    new EmptyResourceType() != null
    new InvalidResourceType() != null
    new UnannotatedBindingTarget().missingResourceAnnotation()
  }
}
