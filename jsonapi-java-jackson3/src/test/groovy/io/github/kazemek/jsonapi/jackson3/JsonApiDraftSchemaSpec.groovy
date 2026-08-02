package io.github.kazemek.jsonapi.jackson3

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.dialect.Dialects

import io.github.kazemek.jsonapi.testfixtures.writer.WriterFixtures

import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

/**
 * Supplemental cross-check of writer-generated documents against the pinned JSON:API 1.1 draft
 * schemas from PR json-api/json-api#1603 (see fixtures/jsonapi-schema/1.1-pr1603/README.md).
 *
 * The draft schemas are unreleased and not an official conformance oracle: a schema result never
 * changes a conformance status in docs/conformance.md. Fixtures that fail the draft only because
 * of a known draft gap are explicitly allow-listed and must keep failing, so a schema fix forces
 * an intentional re-review.
 */
class JsonApiDraftSchemaSpec extends Specification {

  private static final String DRAFT_URI = "https://jsonapi.org/schemas/spec/v1.1/draft"
  private static final String META_SCHEMA_ORIGIN = "https://json-schema.org/"
  private static final String META_SCHEMA_URI = "https://json-schema.org/draft/2020-12/schema"

  private static final List<String> SCHEMA_FILES = [
    "schema.json",
    "schema_create_resource.json",
    "schema_update_resource.json",
    "schema_update_relationship.json",
  ]

  private static final Map<String, String> SCHEMA_KIND_BY_FIXTURE = [
    "single-resource": "response",
    "resource-collection": "response",
    "single-identifier": "response",
    "identifier-collection": "response",
    "null-data": "response",
    "meta-only": "response",
    "empty-identifier-collection": "response",
    "empty-wrappers": "response",
    "empty-errors": "response",
    "empty-included": "response",
    "open-values": "response",
    "relationship-null-linkage": "response",
    "relationship-empty-to-many": "response",
    "relationship-link-only": "response",
    "relationship-meta-only": "response",
    "errors-document": "response",
    "jsonapi-object": "response",
    "compound-document": "response",
    "local-identifier": "create",
    "member-order": "response",
    "extension-and-at-members": "response",
    "string-and-object-links": "response",
  ]

  private static final Map<String, String> ALLOWED_DISAGREEMENTS = [
    "member-order": "response resource carries both id and lid and top-level ext: members; the draft schema requires id and forbids lid in response resources and only models @ members",
    "extension-and-at-members": "top-level ext: member; PR json-api/json-api#1603 does not yet model extension members (see its description)",
    "string-and-object-links": "hreflang canonical list form; draft linkObject.hreflang only accepts a string",
  ]

  private static final List<Map<String, String>> INVALID_CONTROLS = [
    [file: "response-missing-primary.json", kind: "response", keyword: "required", path: ""],
    [file: "create-invalid-lid-type.json", kind: "create", keyword: "type", path: "/data/lid"],
    [file: "update-invalid-missing-id.json", kind: "update", keyword: "required", path: "/data"],
    [file: "update-relationship-invalid-linkage.json", kind: "updateRelationship", keyword: "oneOf", path: "/data"],
  ]

  @Shared
  Path schemaDir = Path.of(System.getProperty("jsonapi.schema.fixtures.dir"))

  @Shared
  JsonMapper mapper = JsonMapper.builder().build()

  @Shared
  SchemaRegistry registry = SchemaRegistry.withDialect(Dialects.getDraft202012(), this.&configureRegistry)

  @Shared
  Map<String, Schema> schemas = [
    response: loadSchema("schema.json"),
    create: loadSchema("schema_create_resource.json"),
    update: loadSchema("schema_update_resource.json"),
    updateRelationship: loadSchema("schema_update_relationship.json"),
  ]

  def "vendored draft schemas match the recorded sha256 pin"() {
    expect:
    sha256sums().every { file, expected ->
      digest(readBytes(file)) == expected
    }
  }

  def "vendored schemas declare the Draft 2020-12 dialect and the draft URI"() {
    expect:
    SCHEMA_FILES.every { file ->
      mapper.readTree(readBytes(file)).get("\$schema").asString() == META_SCHEMA_URI
    }
    mapper.readTree(readBytes("schema.json")).get("\$id").asString() == DRAFT_URI
  }

  def "every writer fixture is classified for a schema kind"() {
    expect:
    WriterFixtures.all().every { it.id in SCHEMA_KIND_BY_FIXTURE }
  }

  def "fixture #fixture.id passes the #kind draft schema"() {
    given:
    def json = JsonApiJackson3.writer(mapper, fixture.context).writeValueAsString(fixture.document)
    def errors = schemas[kind].validate(mapper.readTree(json))

    expect:
    errors.isEmpty()

    where:
    fixture << WriterFixtures.all().findAll {
      it.id in SCHEMA_KIND_BY_FIXTURE && !ALLOWED_DISAGREEMENTS.containsKey(it.id)
    }
    kind = SCHEMA_KIND_BY_FIXTURE[fixture.id]
  }

  def "allow-listed fixture #fixture.id fails only for a documented draft-schema gap"() {
    given:
    def json = JsonApiJackson3.writer(mapper, fixture.context).writeValueAsString(fixture.document)
    def errors = schemas[SCHEMA_KIND_BY_FIXTURE[fixture.id]].validate(mapper.readTree(json))

    expect:
    !errors.isEmpty()
    ALLOWED_DISAGREEMENTS[fixture.id] != null

    where:
    fixture << WriterFixtures.all().findAll { it.id in ALLOWED_DISAGREEMENTS }
  }

  def "invalid control #control.file fails the #control.kind schema at #control.path with #control.keyword"() {
    given:
    def json = mapper.readTree(readText("invalid-controls/" + control.file))
    def errors = schemas[control.kind].validate(json)

    expect:
    errors.any { it.keyword == control.keyword && it.instanceLocation.toString() == control.path }

    where:
    control << INVALID_CONTROLS
  }

  private void configureRegistry(SchemaRegistry.Builder builder) {
    builder
        .schemaLoader { loader ->
          loader.allow { iri ->
            String value = iri.toString()
            value == DRAFT_URI || value.startsWith(META_SCHEMA_ORIGIN)
          }
        }
        .schemas([(DRAFT_URI): readText("schema.json")])
  }

  private Schema loadSchema(String file) {
    return registry.getSchema(readText(file))
  }

  private Map<String, String> sha256sums() {
    return readText("sha256.sum").readLines()
        .findAll { !it.trim().isEmpty() }
        .collectEntries { def parts = it.tokenize(); [(parts[1]): parts[0]] }
  }

  private String digest(byte[] bytes) {
    return MessageDigest.getInstance("SHA-256").digest(bytes).collect { String.format("%02x", it) }.join()
  }

  private String readText(String relativePath) {
    return Files.readString(schemaDir.resolve(relativePath), StandardCharsets.UTF_8)
  }

  private byte[] readBytes(String relativePath) {
    return Files.readAllBytes(schemaDir.resolve(relativePath))
  }
}
