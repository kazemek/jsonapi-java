package io.github.kazemek.jsonapi.testfixtures.codec

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonSlurper
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import spock.lang.Shared
import spock.lang.Specification

class CodecFixturesCatalogSpec extends Specification {

  @Shared
  Path fixturesDir = Path.of(System.getProperty("jsonapi.fixtures.dir"))

  def "catalog ids match manifest and every expected path exists"() {
    given:
    def manifest = new JsonSlurper().parse(fixturesDir.resolve("manifest.json").toFile()) as Map
    def manifestFixtures = manifest.fixtures as List
    def manifestIds = manifestFixtures.collect { it.id as String }
    def catalogIds = CodecFixtures.all()*.id

    expect:
    catalogIds == manifestIds

    and:
    manifestFixtures.each { entry ->
      assert CodecFixtures.byId(entry.id as String).expectedPath == (entry.path as String)
    }

    and:
    CodecFixtures.all().each { fixture ->
      assert Files.isRegularFile(fixturesDir.resolve(fixture.expectedPath))
      if (fixture.assertExactUtf8) {
        assert fixture.exactUtf8Path != null
        assert Files.isRegularFile(fixturesDir.resolve(fixture.exactUtf8Path))
      }
    }
  }

  def "catalog and manifest fixture ids are unique"() {
    given:
    def manifest = new JsonSlurper().parse(fixturesDir.resolve("manifest.json").toFile()) as Map
    def manifestIds = (manifest.fixtures as List).collect { it.id as String }
    def catalogIds = CodecFixtures.all()*.id

    expect:
    catalogIds.size() == catalogIds.toSet().size()
    manifestIds.size() == manifestIds.toSet().size()
  }

  def "byId returns each registered fixture"() {
    expect:
    CodecFixtures.all().every { CodecFixtures.byId(it.id).is(it) }
  }

  def "every fixture is classified for a schema kind"() {
    expect:
    CodecFixtures.all().every { it.schemaKind != null }
  }

  def "schema disagreement requires a schema kind and well-formed expected entries"() {
    expect:
    CodecFixtures.all().every { fixture -> schemaDisagreementValid(fixture) }
  }

  def "primary data kind metadata matches the constructed document data"() {
    expect:
    CodecFixtures.all().every { fixture ->
      def data = fixture.document.data()
      if (data instanceof DocumentData.SingleIdentifier
          || data instanceof DocumentData.IdentifierCollection) {
        fixture.primaryDataKind == PrimaryDataKind.RESOURCE_IDENTIFIER
      } else if (data instanceof DocumentData.SingleResource
          || data instanceof DocumentData.ResourceCollection) {
        fixture.primaryDataKind == PrimaryDataKind.RESOURCE
      } else {
        fixture.primaryDataKind == null
      }
    }
  }

  def "capability selections are non-empty"() {
    expect:
    !CodecFixtures.writable().isEmpty()
    !CodecFixtures.readable().isEmpty()
    !CodecFixtures.schemaChecked().isEmpty()
    !CodecFixtures.exactUtf8().isEmpty()
    !CodecFixtures.hreflangArray().isEmpty()
  }

  def "every fixture participates in at least one codec suite"() {
    expect:
    CodecFixtures.all().every { it.writable || it.readable }
  }

  private static boolean schemaDisagreementValid(CodecFixture fixture) {
    if (fixture.schemaDisagreement == null) {
      return true
    }
    def disagreement = fixture.schemaDisagreement
    def nonBlankString = { value -> value instanceof String && !((String) value).trim().isEmpty() }
    def wellFormedEntries = disagreement.expected.every { entry ->
      nonBlankString(entry['keyword']) && entry['path'] instanceof String
    }
    def hasKind = fixture.schemaKind != null
    def hasReason = nonBlankString(disagreement.reason)
    def hasExpected = disagreement.expected.size() > 0
    return hasKind && hasReason && hasExpected && wellFormedEntries
  }
}
