package io.github.kazemek.jsonapi.testsupport.enveloperead

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import io.github.kazemek.jsonapi.testsupport.codec.CodecScenarios
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticle
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person
import spock.lang.Specification

// Why this spec exists: EnvelopeReadScenarios is the version-neutral typed-envelope catalog shared
// by every Jackson major. Adapter suites run the whole catalog through their own domain document
// reader — Jackson 3 asserts executedScenarioIds == catalogScenarioIds in DomainDocumentReaderSpec,
// and Phase 2.22 mandates the same for Jackson 2 — so every entry must stay self-consistent. These
// tests enforce the local invariants that hold for any catalog entry regardless of catalog size:
// unique stable ids, resolvable codec/binding/core documents, per-variant field invariants
// (document-binding variants require entryPoint and readerContext; registry variants omit both),
// and the FixtureCatalog contract. They fail fast on malformed entries instead of surfacing as
// confusing cross-module test failures.
//
// The catalog grows by addition: adding a scenario is a one-step action that the adapter suites
// pick up automatically. Adapter-specific behavior is documented in the adapter-local specs
// themselves, not enumerated here.
class EnvelopeReadScenariosCatalogSpec extends Specification {

  private static final List<String> INITIAL_INVENTORY = [
    "binds a single-resource document into a flat DTO envelope",
    "binds a homogeneous resource collection in wire order",
    "binds a heterogeneous collection through the registry",
    "preserves explicit null data as NullData",
    "preserves absent data on a meta-only document",
    "passes through identifier primary data without DTO binding",
    "preserves errors without binding anything",
    "preserves jsonapi object, nullable links, and additional members",
    "absent included stays null while present-empty included is a non-null empty IncludedResources",
    "binds included resources preserving wire order with identity lookup",
    "compound shared identity binds one included DTO reachable from both primary resources",
    "shared identity yields one DTO instance reachable from both id and lid keys",
    "fromDocument fails fast on duplicate included identities",
    "unregistered resource-shaped primary fails at the document pointer with null resourceClass",
    "unregistered included type fails at the included index",
    "duplicate registry type names fail at build with the later registrant",
    "registration rejects missing, empty, and invalid resource annotations",
    "binder failures surface with the document pointer joined to the binder path",
    "root-level binder failures join to the document pointer without a trailing slash",
    "cyclic linkage keeps relationship fields as identifiers while included DTOs stay separate",
    "independent envelopes sharing linkage never inject included DTOs",
    "reader-derived envelope collections are mutation-safe"
  ]

  def "catalog ids are unique"() {
    expect:
    EnvelopeReadScenarios.catalog().all()*.id.toSet().size() == EnvelopeReadScenarios.catalog().all().size()
  }

  def "byId returns each registered scenario"() {
    expect:
    EnvelopeReadScenarios.catalog().all().every { EnvelopeReadScenarios.catalog().byId(it.id).is(it) }
  }

  def "initial inventory is the closed shared DomainDocumentReaderSpec names"() {
    expect:
    EnvelopeReadScenarios.catalog().all()*.id == INITIAL_INVENTORY
  }

  def "document-binding variants require entryPoint and readerContext; registry variants omit both"() {
    expect:
    EnvelopeReadScenarios.catalog().all().every { scenario ->
      def variant = scenario.variant()
      if (variant instanceof EnvelopeReadVariant.DocumentBinding) {
        assert variant.entryPoint() != null
        assert variant.cases().every { it.readerContext() != null && it.input() != null && it.expectation() != null }
      } else {
        assert variant instanceof EnvelopeReadVariant.Registry
        assert variant.attempts().every { it.targetClasses() && it.diagnostic() != null && it.resourceClass() != null }
      }
      true
    }
  }

  def "codec-fixture inputs resolve in CodecScenarios"() {
    expect:
    EnvelopeReadScenarios.catalog().all().each { scenario ->
      def variant = scenario.variant()
      if (variant instanceof EnvelopeReadVariant.DocumentBinding) {
        variant.cases().each { envelopeCase ->
          def input = envelopeCase.input()
          if (input instanceof EnvelopeReadInput.CodecFixture) {
            assert CodecScenarios.catalog().byId(input.codecScenarioId()).id() == input.codecScenarioId()
          }
        }
      }
    }
  }

  def "named binding variants and fromDocument wire forms resolve under envelope-binding"() {
    expect:
    EnvelopeBindingDocument.values().every { document ->
      TestSupportResources.corpusExists(document.relativePath())
    }
    EnvelopeReadScenarios.catalog().all().each { scenario ->
      def variant = scenario.variant()
      if (variant instanceof EnvelopeReadVariant.DocumentBinding) {
        variant.cases().each { envelopeCase ->
          def input = envelopeCase.input()
          if (input instanceof EnvelopeReadInput.BindingDocument) {
            assert TestSupportResources.corpusExists(input.document().relativePath())
          }
          if (input instanceof EnvelopeReadInput.CoreDocument) {
            assert TestSupportResources.corpusExists(input.wireForm().relativePath())
            assert input.document() != null
          }
        }
      }
    }
  }

  def "only the duplicate-identity wire form is validation-invalid"() {
    expect:
    EnvelopeBindingDocument.values().findAll { it.validationInvalid() } == [
      EnvelopeBindingDocument.DUPLICATE_INCLUDED_IDENTITIES
    ]
  }

  def "fromDocument cases pin CoreDocument inputs and resourceDefaults"() {
    expect:
    EnvelopeReadScenarios.catalog().all().each { scenario ->
      def variant = scenario.variant()
      if (variant instanceof EnvelopeReadVariant.DocumentBinding
          && variant.entryPoint() == EnvelopeEntryPoint.FROM_DOCUMENT) {
        variant.cases().each { envelopeCase ->
          assert envelopeCase.input() instanceof EnvelopeReadInput.CoreDocument
          assert envelopeCase.readerContext() == EnvelopeReaderContext.RESOURCE_DEFAULTS
        }
      }
    }
  }

  def "CODEC_DERIVED is used only with codec-fixture inputs"() {
    expect:
    EnvelopeReadScenarios.catalog().all().each { scenario ->
      def variant = scenario.variant()
      if (variant instanceof EnvelopeReadVariant.DocumentBinding) {
        variant.cases().each { envelopeCase ->
          if (envelopeCase.readerContext() == EnvelopeReaderContext.CODEC_DERIVED) {
            assert envelopeCase.input() instanceof EnvelopeReadInput.CodecFixture
          }
        }
      }
    }
  }

  def "identifier pass-through pins identifierDefaults on both codec inputs"() {
    given:
    def scenario = EnvelopeReadScenarios.catalog().byId("passes through identifier primary data without DTO binding")
    def binding = (EnvelopeReadVariant.DocumentBinding) scenario.variant()

    expect:
    binding.cases().every {
      it.readerContext() == EnvelopeReaderContext.IDENTIFIER_DEFAULTS
      it.input() instanceof EnvelopeReadInput.CodecFixture
    }
    binding.cases()*.input()*.codecScenarioId() == [
      "single-identifier",
      "identifier-collection"
    ]
  }

  def "target types live in shared fixture packages"() {
    expect:
    EnvelopeReadScenarios.catalog().all().each { scenario ->
      def variant = scenario.variant()
      def classes = []
      if (variant instanceof EnvelopeReadVariant.DocumentBinding) {
        classes.addAll(variant.targetClasses())
      } else {
        variant.attempts().each { classes.addAll(it.targetClasses()) }
      }
      classes.each { type ->
        def pkg = type.packageName
        assert pkg == "io.github.kazemek.jsonapi.testsupport.fixtures.enveloperead" ||
            pkg == "io.github.kazemek.jsonapi.testsupport.fixtures.domainread" ||
            pkg == "io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite"
      }
    }
  }

  def "where with a matching predicate returns the full catalog and a rejecting predicate is empty"() {
    expect:
    EnvelopeReadScenarios.catalog().where({ true })*.id == EnvelopeReadScenarios.catalog().all()*.id
    EnvelopeReadScenarios.catalog().where({ false }).isEmpty()
  }

  def "byId rejects unknown ids"() {
    when:
    EnvelopeReadScenarios.catalog().byId("no such scenario")

    then:
    thrown(IllegalArgumentException)
  }

  def "document-binding rejects an empty case list"() {
    when:
    new EnvelopeReadVariant.DocumentBinding(
        List.of(FlatArticle.class), EnvelopeEntryPoint.READ_VALUE, List.of())

    then:
    thrown(IllegalArgumentException)
  }

  def "fromDocument scenario rejects a non-core input"() {
    when:
    new EnvelopeReadScenario(
        "bad",
        new EnvelopeReadVariant.DocumentBinding(
        List.of(FlatArticle.class),
        EnvelopeEntryPoint.FROM_DOCUMENT,
        List.of(
        new EnvelopeReadCase(
        EnvelopeReadInput.binding(EnvelopeBindingDocument.SINGLE_RESOURCE),
        EnvelopeReaderContext.RESOURCE_DEFAULTS,
        EnvelopeReadExpectation.failure(MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE, "/data")))))

    then:
    thrown(IllegalArgumentException)
  }

  def "CODEC_DERIVED rejects a binding-document input"() {
    when:
    new EnvelopeReadScenario(
        "bad",
        new EnvelopeReadVariant.DocumentBinding(
        List.of(FlatArticle.class),
        EnvelopeEntryPoint.READ_VALUE,
        List.of(
        new EnvelopeReadCase(
        EnvelopeReadInput.binding(EnvelopeBindingDocument.SINGLE_RESOURCE),
        EnvelopeReaderContext.CODEC_DERIVED,
        EnvelopeReadExpectation.failure(MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE, "/data")))))

    then:
    thrown(IllegalArgumentException)
  }

  def "readValue scenario rejects a CoreDocument input"() {
    when:
    new EnvelopeReadScenario(
        "bad",
        new EnvelopeReadVariant.DocumentBinding(
        List.of(Person.class),
        EnvelopeEntryPoint.READ_VALUE,
        List.of(
        new EnvelopeReadCase(
        EnvelopeReadInput.core(
        EnvelopeBindingDocument.SHARED_IDENTITY_ID_AND_LID,
        new JsonApiDocument(
        new DocumentData.SingleResource(ResourceObject.of("articles", "1")),
        null, null, null, null, null, Map.of())),
        EnvelopeReaderContext.RESOURCE_DEFAULTS,
        EnvelopeReadExpectation.failure(MappingDiagnostic.UNREGISTERED_RESOURCE_TYPE, "/data")))))

    then:
    thrown(IllegalArgumentException)
  }

  def "registry rejects an empty attempt list"() {
    when:
    new EnvelopeReadVariant.Registry(List.of())

    then:
    thrown(IllegalArgumentException)
  }
}
