package io.github.kazemek.jsonapi.jackson3

import com.networknt.schema.Schema
import com.networknt.schema.SchemaRegistry
import com.networknt.schema.dialect.Dialects

import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import io.github.kazemek.jsonapi.testsupport.codec.CodecScenarios
import io.github.kazemek.jsonapi.testsupport.codec.SchemaKind

import spock.lang.Shared
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

/**
 * Adapter-local cross-check of writer-generated documents against the pinned JSON:API 1.1 draft
 * schemas owned by {@code jsonapi-java-test-support}. Pin/integrity of those resources lives in
 * the test-support module; this spec only executes output-versus-schema checks.
 *
 * The draft schemas are unreleased and not an official conformance oracle: a schema result never
 * changes a conformance status in docs/conformance.md. Fixtures that fail the draft only because
 * of a known draft gap carry {@link io.github.kazemek.jsonapi.testsupport.codec.SchemaDisagreement}
 * metadata and must keep failing, so a schema fix forces an intentional re-review.
 */
class JsonApiDraftSchemaSpec extends Specification {

  private static final String DRAFT_URI = "https://jsonapi.org/schemas/spec/v1.1/draft"
  private static final String META_SCHEMA_ORIGIN = "https://json-schema.org/"

  private static final Map<SchemaKind, String> SCHEMA_FILE_BY_KIND = [
    (SchemaKind.RESPONSE): "schema.json",
    (SchemaKind.CREATE): "schema_create_resource.json",
    (SchemaKind.UPDATE): "schema_update_resource.json",
    (SchemaKind.UPDATE_RELATIONSHIP): "schema_update_relationship.json",
  ]

  private static final List<Map<String, String>> INVALID_CONTROLS = [
    [file: "response-missing-primary.json", kind: "response", keyword: "required", path: ""],
    [file: "create-invalid-lid-type.json", kind: "create", keyword: "type", path: "/data/lid"],
    [file: "update-invalid-missing-id.json", kind: "update", keyword: "required", path: "/data"],
    [file: "update-relationship-invalid-linkage.json", kind: "updateRelationship", keyword: "oneOf", path: "/data"],
  ]

  @Shared
  JsonMapper mapper = JsonMapper.builder().build()

  @Shared
  SchemaRegistry registry = SchemaRegistry.withDialect(Dialects.getDraft202012(), this.&configureRegistry)

  @Shared
  Map<SchemaKind, Schema> schemas = SCHEMA_FILE_BY_KIND.collectEntries { kind, file ->
    [(kind): loadSchema(file)]
  }

  def "every schema-checked fixture declares a known schema kind"() {
    expect:
    CodecScenarios.catalog().where { it.schemaKind() != null }.every { it.schemaKind in SCHEMA_FILE_BY_KIND }
  }

  def "fixture #fixture.id passes the #kind draft schema"() {
    given:
    def errors = errorsFor(fixture)

    expect:
    errors.isEmpty()

    where:
    fixture << CodecScenarios.catalog().where { it.schemaKind() != null }.findAll { it.schemaDisagreement == null }
    kind = fixture.schemaKind
  }

  def "allow-listed fixture #fixture.id fails for the documented draft-schema gap"() {
    given:
    def disagreement = fixture.schemaDisagreement
    def errors = errorsFor(fixture)
    def observed = errors.collect { [keyword: it.keyword, path: it.instanceLocation.toString()] }

    expect:
    disagreement.expected.every { expected ->
      observed.any { it.keyword == expected.keyword && it.path == expected.path }
    }

    where:
    fixture << CodecScenarios.catalog().where { it.schemaKind() != null }.findAll { it.schemaDisagreement != null }
  }

  def "invalid control #control.file fails the #control.kind schema at #control.path with #control.keyword"() {
    given:
    def json = mapper.readTree(TestSupportResources.readSchemaBytes("invalid-controls/" + control.file))
    def errors = schemas[schemaKindFor(control.kind)].validate(json)

    expect:
    errors.any { it.keyword == control.keyword && it.instanceLocation.toString() == control.path }

    where:
    control << INVALID_CONTROLS
  }

  private List<?> errorsFor(fixture) {
    def json = JsonApiJackson3.writer(mapper, fixture.context).writeValueAsString(fixture.document)
    return schemas[fixture.schemaKind].validate(mapper.readTree(json))
  }

  private static final Map<String, SchemaKind> SCHEMA_KIND_BY_NAME = [
    'response': SchemaKind.RESPONSE,
    'create': SchemaKind.CREATE,
    'update': SchemaKind.UPDATE,
    'updateRelationship': SchemaKind.UPDATE_RELATIONSHIP,
  ]

  private static SchemaKind schemaKindFor(String name) {
    def kind = SCHEMA_KIND_BY_NAME[name]
    if (kind == null) {
      throw new IllegalArgumentException('Unknown schema kind name: ' + name)
    }
    return kind
  }

  private void configureRegistry(SchemaRegistry.Builder builder) {
    builder
        .schemaLoader { loader ->
          loader.allow { iri ->
            String value = iri.toString()
            value == DRAFT_URI || value.startsWith(META_SCHEMA_ORIGIN)
          }
        }
        .schemas([(DRAFT_URI): TestSupportResources.readSchemaUtf8("schema.json")])
  }

  private Schema loadSchema(String file) {
    return registry.getSchema(TestSupportResources.readSchemaUtf8(file))
  }
}
