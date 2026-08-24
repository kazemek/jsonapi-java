package io.github.kazemek.jsonapi.testsupport.codec

import groovy.json.JsonSlurper
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind
import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import spock.lang.Specification

class CodecScenariosCatalogSpec extends Specification {

  def "catalog ids match manifest and every expected path exists"() {
    given:
    def manifest = new JsonSlurper().parseText(TestSupportResources.readCorpusUtf8("manifest.json")) as Map
    def manifestFixtures = manifest.fixtures as List
    def manifestIds = manifestFixtures.collect { it.id as String }
    def catalogIds = CodecScenarios.catalog().all()*.id

    expect:
    catalogIds == manifestIds

    and:
    manifestFixtures.each { entry ->
      def scenario = CodecScenarios.catalog().byId(entry.id as String)
      assert scenario.expectedPath == (entry.path as String)
      assert scenario.notes == (entry.notes as String)
    }

    and:
    CodecScenarios.catalog().all().each { fixture ->
      assert TestSupportResources.corpusExists(fixture.expectedPath)
      if (fixture.assertExactUtf8) {
        assert fixture.exactUtf8Path != null
        assert TestSupportResources.corpusExists(fixture.exactUtf8Path)
      }
    }
  }

  def "catalog and manifest fixture ids are unique"() {
    given:
    def manifest = new JsonSlurper().parseText(TestSupportResources.readCorpusUtf8("manifest.json")) as Map
    def manifestIds = (manifest.fixtures as List).collect { it.id as String }
    def catalogIds = CodecScenarios.catalog().all()*.id

    expect:
    catalogIds.size() == catalogIds.toSet().size()
    manifestIds.size() == manifestIds.toSet().size()
  }

  def "byId returns each registered fixture"() {
    expect:
    CodecScenarios.catalog().all().every { CodecScenarios.catalog().byId(it.id).is(it) }
  }

  def "every fixture is classified for a schema kind"() {
    expect:
    CodecScenarios.catalog().all().every { it.schemaKind != null }
  }

  def "schema disagreement requires a schema kind and well-formed expected entries"() {
    expect:
    CodecScenarios.catalog().all().every { fixture -> schemaDisagreementValid(fixture) }
  }

  def "primary data kind metadata matches the constructed document data"() {
    expect:
    CodecScenarios.catalog().all().every { fixture ->
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
    !CodecScenarios.catalog().where { it.writable }.isEmpty()
    !CodecScenarios.catalog().where { it.readable }.isEmpty()
    !CodecScenarios.catalog().where { it.schemaKind() != null }.isEmpty()
    !CodecScenarios.catalog().where { it.assertExactUtf8 }.isEmpty()
    !CodecScenarios.catalog().where { it.assertHreflangArray }.isEmpty()
  }

  def "every fixture participates in at least one codec suite"() {
    expect:
    CodecScenarios.catalog().all().every { it.writable || it.readable }
  }

  def "byId rejects unknown ids with the unified message"() {
    when:
    CodecScenarios.catalog().byId("no-such-scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown codec scenario id: no-such-scenario"
  }

  private static boolean schemaDisagreementValid(CodecScenario fixture) {
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
