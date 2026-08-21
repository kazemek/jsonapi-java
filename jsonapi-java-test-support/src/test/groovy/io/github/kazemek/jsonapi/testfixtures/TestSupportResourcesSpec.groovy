package io.github.kazemek.jsonapi.testfixtures

import java.nio.charset.StandardCharsets
import spock.lang.Specification

class TestSupportResourcesSpec extends Specification {

  def "corpus manifest is available on the classpath"() {
    expect:
    TestSupportResources.corpusExists("manifest.json")
    TestSupportResources.readCorpusUtf8("manifest.json").contains('"fixtures"')
  }

  def "vendored schema pin is available on the classpath"() {
    expect:
    TestSupportResources.schemaExists("schema.json")
    TestSupportResources.schemaExists("sha256.sum")
    TestSupportResources.readSchemaUtf8("schema.json").contains('"$id"')
  }

  def "exact-byte corpus reads preserve UTF-8 bytes"() {
    given:
    def relativePath = "documents/member-order.compact.json"
    def bytes = TestSupportResources.readCorpusBytes(relativePath)

    expect:
    bytes.length > 0
    TestSupportResources.readCorpusUtf8(relativePath) == new String(bytes, StandardCharsets.UTF_8)
    TestSupportResources.openCorpus(relativePath).readAllBytes() == bytes
    TestSupportResources.openSchema("schema.json").readAllBytes() == TestSupportResources.readSchemaBytes("schema.json")
  }

  def "exists checks return false for missing resources"() {
    expect:
    !TestSupportResources.corpusExists("documents/does-not-exist.json")
    !TestSupportResources.schemaExists("missing-schema.json")
  }

  def "missing corpus resource fails with the classpath path"() {
    when:
    TestSupportResources.readCorpusBytes("documents/does-not-exist.json")

    then:
    def ex = thrown(IllegalStateException)
    ex.message == "Missing test-support classpath resource: jsonapi/corpus/1.1/documents/does-not-exist.json"
  }

  def "missing schema resource fails with the classpath path"() {
    when:
    TestSupportResources.readSchemaBytes("missing-schema.json")

    then:
    def ex = thrown(IllegalStateException)
    ex.message == "Missing test-support classpath resource: jsonapi/schema/vendor/1.1-pr1603/missing-schema.json"
  }

  def "relative paths reject traversal and absolute forms"() {
    when:
    TestSupportResources.readCorpusBytes(path)

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Invalid test-support resource path: " + path

    where:
    path << [
      "/manifest.json",
      "\\manifest.json",
      "",
      "../manifest.json",
      "documents/../manifest.json"
    ]
  }
}
