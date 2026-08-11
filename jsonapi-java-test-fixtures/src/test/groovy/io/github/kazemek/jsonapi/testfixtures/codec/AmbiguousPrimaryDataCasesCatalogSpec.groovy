package io.github.kazemek.jsonapi.testfixtures.codec

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

class AmbiguousPrimaryDataCasesCatalogSpec extends Specification {

  private static final List<String> AMBIGUOUS_IDS = [
    'ambiguous-object-primary-data',
    'ambiguous-empty-array-primary-data',
  ]

  @Shared
  Path fixturesDir = Path.of(System.getProperty("jsonapi.fixtures.dir"))

  def "ambiguous catalog is exactly the two dual-success cases"() {
    expect:
    AmbiguousPrimaryDataCases.all()*.id == AMBIGUOUS_IDS
  }

  def "manifest entries match the catalog and inputs exist"() {
    given:
    def manifest = new JsonSlurper().parse(fixturesDir.resolve("ambiguous-manifest.json").toFile()) as Map
    def manifestCases = manifest.cases as List

    expect:
    manifestCases*.id == AmbiguousPrimaryDataCases.all()*.id

    and:
    AmbiguousPrimaryDataCases.all().each { entry ->
      def manifestEntry = manifestCases.find { it.id == entry.id }
      assert manifestEntry.path == entry.expectedPath
      assert Files.isRegularFile(fixturesDir.resolve(entry.expectedPath))
    }
  }

  def "byId returns each registered case"() {
    expect:
    AmbiguousPrimaryDataCases.all().every { AmbiguousPrimaryDataCases.byId(it.id).is(it) }
  }
}
