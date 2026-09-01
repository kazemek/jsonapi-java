package io.github.kazemek.jsonapi.testsupport.domainread

import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingLocation
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticle
import spock.lang.Specification

class DomainReadVerifierSpec extends Specification {

  def "bound value success compares the catalog DTO"() {
    given:
    def expected = new FlatArticle("1", "T", null, null, null)
    def scenario = bound("ok", expected)

    when:
    DomainReadVerifier.verify(scenario, expected, null)

    then:
    noExceptionThrown()
  }

  def "included-isolation success requires two equal bound values"() {
    given:
    def expected = new FlatArticle("1", "T", null, null, null)
    def scenario = new DomainReadScenario(
        "isolation",
        DomainReadInput.includedIsolation("{\"a\":1}", "{\"a\":1,\"included\":[]}"),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.bound(expected))

    when:
    DomainReadVerifier.verify(scenario, [expected, expected], null)

    then:
    noExceptionThrown()

    when:
    DomainReadVerifier.verify(scenario, [expected], null)

    then:
    thrown(AssertionError)
  }

  def "bound collection compares each element"() {
    given:
    def first = new FlatArticle("1", "A", null, null, null)
    def second = new FlatArticle("2", "B", null, null, null)
    def scenario = new DomainReadScenario(
        "collection",
        DomainReadInput.collection([resource("1"), resource("2")]),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.bound([first, second]))

    when:
    DomainReadVerifier.verify(scenario, [first, second], null)

    then:
    noExceptionThrown()
  }

  def "failure expectation asserts diagnostic and optional path"() {
    given:
    def scenario = new DomainReadScenario(
        "mismatch",
        DomainReadInput.single(resource("1")),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.failure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH, "/type", FlatArticle))
    def mappingException = new JsonApiMappingException(
        MappingDiagnostic.RESOURCE_TYPE_MISMATCH,
        FlatArticle,
        MappingLocation.parse("/type"))

    when:
    DomainReadVerifier.verify(scenario, null, mappingException)

    then:
    noExceptionThrown()

    when:
    DomainReadVerifier.verify(scenario, "bound", null)

    then:
    thrown(AssertionError)
  }

  def "bound value mismatch fails"() {
    given:
    def scenario = bound("diff", new FlatArticle("1", "T", null, null, null))

    when:
    DomainReadVerifier.verify(scenario, new FlatArticle("1", "Other", null, null, null), null)

    then:
    thrown(AssertionError)
  }

  def "failure with a non-mapping exception is rejected"() {
    given:
    def scenario = new DomainReadScenario(
        "not-mapping",
        DomainReadInput.single(resource("1")),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.failure(MappingDiagnostic.RESOURCE_TYPE_MISMATCH))

    when:
    DomainReadVerifier.verify(scenario, null, new IllegalStateException("nope"))

    then:
    thrown(AssertionError)
  }

  def "success with an unexpected throwable is rejected"() {
    given:
    def scenario = bound("surprise", new FlatArticle("1", "T", null, null, null))

    when:
    DomainReadVerifier.verify(scenario, null, new IllegalStateException("boom"))

    then:
    thrown(AssertionError)
  }

  def "included-isolation requires a two-element list"() {
    given:
    def expected = new FlatArticle("1", "T", null, null, null)
    def scenario = new DomainReadScenario(
        "isolation-shape",
        DomainReadInput.includedIsolation("{\"a\":1}", "{\"a\":1,\"included\":[]}"),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.bound(expected))

    when:
    DomainReadVerifier.verify(scenario, expected, null)

    then:
    thrown(AssertionError)
  }

  def "bound collection rejects a non-list and a size mismatch"() {
    given:
    def first = new FlatArticle("1", "A", null, null, null)
    def second = new FlatArticle("2", "B", null, null, null)
    def scenario = new DomainReadScenario(
        "collection-shape",
        DomainReadInput.collection([resource("1"), resource("2")]),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.bound([first, second]))

    when:
    DomainReadVerifier.verify(scenario, first, null)

    then:
    thrown(AssertionError)

    when:
    DomainReadVerifier.verify(scenario, [first], null)

    then:
    thrown(AssertionError)
  }

  def "failure without a shared path still matches diagnostic-only expectations"() {
    given:
    def scenario = new DomainReadScenario(
        "diagnostic-only",
        DomainReadInput.single(resource("1")),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.failure(MappingDiagnostic.MISSING_IDENTIFIER))
    def mappingException = new JsonApiMappingException(
        MappingDiagnostic.MISSING_IDENTIFIER, FlatArticle, MappingLocation.parse("/id"))

    when:
    DomainReadVerifier.verify(scenario, null, mappingException)

    then:
    noExceptionThrown()
  }

  private static DomainReadScenario bound(String id, FlatArticle value) {
    return new DomainReadScenario(
        id,
        DomainReadInput.single(resource(value.id())),
        FlatArticle,
        ConverterBehavior.DEFAULT_CONVERT_VALUE,
        DomainReadExpectation.bound(value))
  }

  private static ResourceObject resource(String id) {
    return ResourceObject.of("articles", id)
  }
}
