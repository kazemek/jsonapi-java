package io.github.kazemek.jsonapi.testfixtures.codec

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonSlurper
import io.github.kazemek.jsonapi.testfixtures.FixtureDirectory
import spock.lang.Shared
import spock.lang.Specification

class AmbiguousPrimaryDataScenariosCatalogSpec extends Specification {

  private static final List<String> AMBIGUOUS_IDS = [
    'ambiguous-object-primary-data',
    'ambiguous-empty-array-primary-data',
  ]

  @Shared
  Path fixturesDir = FixtureDirectory.jsonApiFixtures()

  def "ambiguous catalog is exactly the two dual-success cases"() {
    expect:
    AmbiguousPrimaryDataScenarios.all()*.id == AMBIGUOUS_IDS
  }

  def "manifest entries match the catalog and inputs exist"() {
    given:
    def manifest = new JsonSlurper().parse(fixturesDir.resolve("ambiguous-manifest.json").toFile()) as Map
    def manifestCases = manifest.cases as List

    expect:
    manifestCases*.id == AmbiguousPrimaryDataScenarios.all()*.id

    and:
    AmbiguousPrimaryDataScenarios.all().each { entry ->
      def manifestEntry = manifestCases.find { it.id == entry.id }
      assert manifestEntry.path == entry.expectedPath
      assert Files.isRegularFile(fixturesDir.resolve(entry.expectedPath))
    }
  }

  def "byId returns each registered case"() {
    expect:
    AmbiguousPrimaryDataScenarios.all().every { AmbiguousPrimaryDataScenarios.byId(it.id).is(it) }
  }

  def "where shim filters the catalog"() {
    expect:
    AmbiguousPrimaryDataScenarios.where({ true })*.id == AmbiguousPrimaryDataScenarios.all()*.id
    AmbiguousPrimaryDataScenarios.where({ false }).isEmpty()
  }

  def "byId rejects unknown ids with the unified message"() {
    when:
    AmbiguousPrimaryDataScenarios.byId("no-such-scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown ambiguous-primary-data scenario id: no-such-scenario"
  }
}
