package io.github.kazemek.jsonapi.testfixtures.schema

import java.security.MessageDigest

import groovy.json.JsonSlurper
import io.github.kazemek.jsonapi.testfixtures.TestSupportResources
import spock.lang.Specification

/**
 * Pin and integrity checks for the vendored JSON:API 1.1 draft-PR schemas. Adapter suites consume
 * the same classpath resources for output-versus-schema execution; they do not re-own the pin.
 */
class PinnedJsonApiSchemaResourcesSpec extends Specification {

  private static final String DRAFT_URI = "https://jsonapi.org/schemas/spec/v1.1/draft"
  private static final String META_SCHEMA_URI = "https://json-schema.org/draft/2020-12/schema"
  private static final String PINNED_COMMIT = "4ee1c644fcc273044ecec39a6b8c0f0485abdc0e"

  private static final List<String> SCHEMA_FILES = [
    "schema.json",
    "schema_create_resource.json",
    "schema_update_resource.json",
    "schema_update_relationship.json",
  ]

  private static final List<String> INVALID_CONTROLS = [
    "invalid-controls/response-missing-primary.json",
    "invalid-controls/create-invalid-lid-type.json",
    "invalid-controls/update-invalid-missing-id.json",
    "invalid-controls/update-relationship-invalid-linkage.json",
  ]

  def "vendored draft schemas match the recorded sha256 pin"() {
    given:
    def checksums = sha256sums()

    expect:
    checksums.keySet() == SCHEMA_FILES.toSet()
    SCHEMA_FILES.every { file ->
      digest(TestSupportResources.readSchemaBytes(file)) == checksums[file]
    }
  }

  def "vendored schemas declare the Draft 2020-12 dialect and the draft URI"() {
    given:
    def slurper = new JsonSlurper()

    expect:
    SCHEMA_FILES.every { file ->
      slurper.parseText(TestSupportResources.readSchemaUtf8(file)).'$schema' == META_SCHEMA_URI
    }
    slurper.parseText(TestSupportResources.readSchemaUtf8("schema.json")).'$id' == DRAFT_URI
  }

  def "pin metadata records the upstream commit and repository-authored invalid controls exist"() {
    given:
    def readme = TestSupportResources.readSchemaUtf8("README.md")

    expect:
    readme.contains(PINNED_COMMIT)
    INVALID_CONTROLS.every { TestSupportResources.schemaExists(it) }
  }

  private static Map<String, String> sha256sums() {
    Map<String, String> checksums = [:]
    TestSupportResources.readSchemaUtf8("sha256.sum").readLines()
        .findAll { line -> !line.trim().isEmpty() }
        .each { line ->
          def parts = line.tokenize()
          checksums.put(parts[1], parts[0])
        }
    return checksums
  }

  private static String digest(byte[] bytes) {
    return MessageDigest.getInstance("SHA-256").digest(bytes).collect { String.format("%02x", it) }.join()
  }
}
