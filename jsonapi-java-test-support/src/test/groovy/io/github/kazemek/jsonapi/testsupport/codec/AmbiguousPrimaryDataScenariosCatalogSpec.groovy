package io.github.kazemek.jsonapi.testsupport.codec

import groovy.json.JsonSlurper
import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import spock.lang.Specification

class AmbiguousPrimaryDataScenariosCatalogSpec extends Specification {

  private static final List<String> AMBIGUOUS_IDS = [
    'ambiguous-object-primary-data',
    'ambiguous-empty-array-primary-data',
  ]

  def "ambiguous catalog is exactly the two dual-success cases"() {
    expect:
    AmbiguousPrimaryDataScenarios.catalog().all()*.id == AMBIGUOUS_IDS
  }

  def "manifest entries match the catalog and inputs exist"() {
    given:
    def manifest = new JsonSlurper().parseText(TestSupportResources.readCorpusUtf8("ambiguous-manifest.json")) as Map
    def manifestCases = manifest.cases as List

    expect:
    manifestCases*.id == AmbiguousPrimaryDataScenarios.catalog().all()*.id

    and:
    AmbiguousPrimaryDataScenarios.catalog().all().each { entry ->
      def manifestEntry = manifestCases.find { it.id == entry.id }
      assert manifestEntry.path == entry.expectedPath
      assert TestSupportResources.corpusExists(entry.expectedPath)
    }
  }

  def "byId returns each registered case"() {
    expect:
    AmbiguousPrimaryDataScenarios.catalog().all().every { AmbiguousPrimaryDataScenarios.catalog().byId(it.id).is(it) }
  }

  def "where shim filters the catalog"() {
    expect:
    AmbiguousPrimaryDataScenarios.catalog().where({ true })*.id == AmbiguousPrimaryDataScenarios.catalog().all()*.id
    AmbiguousPrimaryDataScenarios.catalog().where({ false }).isEmpty()
  }

  def "byId rejects unknown ids with the unified message"() {
    when:
    AmbiguousPrimaryDataScenarios.catalog().byId("no-such-scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown ambiguous-primary-data scenario id: no-such-scenario"
  }
}
