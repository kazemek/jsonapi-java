package io.github.kazemek.jsonapi.testsupport.codec

import groovy.json.JsonSlurper
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.jackson.document.PrimaryDataKind
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

  def "manifest fixture ids are unique"() {
    given:
    def manifest = new JsonSlurper().parseText(TestSupportResources.readCorpusUtf8("manifest.json")) as Map
    def manifestIds = (manifest.fixtures as List).collect { it.id as String }

    expect:
    manifestIds.size() == manifestIds.toSet().size()
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

  def "capability metadata is pinned per scenario id"() {
    given:
    def response = capabilities(SchemaKind.RESPONSE)
    def expected = [
      "single-resource"                 : response,
      "resource-collection"             : response,
      "single-identifier"               : response,
      "identifier-collection"           : response,
      "null-data"                       : response,
      "meta-only"                       : response,
      "empty-identifier-collection"     : response,
      "empty-wrappers"                  : response,
      "empty-errors"                    : response,
      "empty-included"                  : response,
      "open-values"                     : response,
      "relationship-null-linkage"       : response,
      "relationship-empty-to-many"      : response,
      "relationship-link-only"          : response,
      "relationship-meta-only"          : response,
      "string-and-object-links"         : capabilities(SchemaKind.RESPONSE, false, null, true),
      "errors-document"                 : response,
      "jsonapi-object"                  : response,
      "compound-document"               : response,
      "compound-nested-intermediate"    : response,
      "compound-shared-identity"        : response,
      "local-identifier"                : capabilities(SchemaKind.CREATE),
      "extension-and-at-members"        : response,
      "member-order"                    : capabilities(SchemaKind.RESPONSE, true, "documents/member-order.compact.json")
    ]
    def actual = CodecScenarios.catalog().all().collectEntries { fixture ->
      [(fixture.id): capabilities(
        fixture.schemaKind,
        fixture.assertExactUtf8,
        fixture.exactUtf8Path,
        fixture.assertHreflangArray,
        fixture.writable,
        fixture.readable)]
    }

    expect:
    actual == expected
  }

  def "every fixture participates in at least one codec suite"() {
    expect:
    CodecScenarios.catalog().all().every { it.writable || it.readable }
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

  private static Map<String, Object> capabilities(
      SchemaKind schemaKind,
      boolean assertExactUtf8 = false,
      String exactUtf8Path = null,
      boolean assertHreflangArray = false,
      boolean writable = true,
      boolean readable = true) {
    [
      writable            : writable,
      readable            : readable,
      schemaKind          : schemaKind,
      assertExactUtf8     : assertExactUtf8,
      exactUtf8Path       : exactUtf8Path,
      assertHreflangArray : assertHreflangArray
    ] as Map<String, Object>
  }
}
