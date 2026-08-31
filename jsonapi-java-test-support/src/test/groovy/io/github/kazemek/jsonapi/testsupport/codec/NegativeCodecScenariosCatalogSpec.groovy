package io.github.kazemek.jsonapi.testsupport.codec

import groovy.json.JsonSlurper
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CodecFailureCategory
import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import spock.lang.Specification

class NegativeCodecScenariosCatalogSpec extends Specification {

  private static final List<String> CLOSED_IDS = [
    'malformed-json-without-payload',
    'truncated-document-enclosing-path',
    'empty-input',
    'trailing-content-after-document',
    'unexpected-token-path-location',
    'duplicate-members',
    'local-validation-top-level',
    'included-missing-type',
    'collection-missing-type',
    'relationship-identifier-missing-type',
    'reserved-attribute',
    'missing-link-href',
    'invalid-dynamic-link-relation',
    'invalid-dynamic-attribute-name',
    'aggregate-uri-link-relation',
    'aggregate-validation-resource-location',
    'extension-members-require-context',
  ]

  def "negative corpus is exactly the closed Phase 2.4 case set without duplicates"() {
    expect:
    NegativeCodecScenarios.catalog().all()*.id == CLOSED_IDS
  }

  def "manifest entries match the loaded corpus and every input exists"() {
    given:
    def manifest = new JsonSlurper().parseText(TestSupportResources.readCorpusUtf8("negative-manifest.json")) as Map
    def manifestCases = manifest.cases as List

    expect:
    manifestCases*.id == NegativeCodecScenarios.catalog().all()*.id

    and:
    NegativeCodecScenarios.catalog().all().each { entry ->
      def manifestEntry = manifestCases.find { it.id == entry.id }
      assert manifestEntry.path == entry.path
      assert manifestEntry.category == entry.category
      assert (manifestEntry.pointer as String) == entry.pointer
      assert (manifestEntry.ruleCode as String) == entry.ruleCode
      assert (manifestEntry.sourceLocation ?: false) as boolean == entry.sourceLocation
      assert TestSupportResources.corpusExists(entry.path)
    }
  }

  def "every negative case records a known category and a valid pointer"() {
    expect:
    NegativeCodecScenarios.catalog().all().every { entry -> categoryAndPointerValid(entry) }
  }

  def "rule codes appear exactly for validation categories and are known codes"() {
    expect:
    NegativeCodecScenarios.catalog().all().every { entry -> ruleCodeValid(entry) }
  }

  private static boolean categoryAndPointerValid(NegativeCodecScenario entry) {
    def knownCategory = CodecFailureCategory.values().any { it.name() == entry.category }
    return knownCategory && validJsonPointer(entry.pointer)
  }

  private static boolean validJsonPointer(String pointer) {
    if (pointer == null || pointer.isEmpty()) {
      return true
    }
    if (!pointer.startsWith('/')) {
      return false
    }
    def i = 0
    while (i < pointer.length()) {
      if (pointer.charAt(i) != '~') {
        i++
        continue
      }
      if (i + 1 >= pointer.length()) {
        return false
      }
      def escape = pointer.charAt(i + 1)
      if (escape != '0' && escape != '1') {
        return false
      }
      i += 2
    }
    return true
  }

  private static boolean ruleCodeValid(NegativeCodecScenario entry) {
    def validation = entry.category == 'LOCAL_VALIDATION' || entry.category == 'AGGREGATE_VALIDATION'
    if (entry.ruleCode == null) {
      return !validation
    }
    def knownCode = ValidationRuleCode.values().any { it.name() == entry.ruleCode }
    return validation && knownCode
  }
}
