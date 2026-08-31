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
}
