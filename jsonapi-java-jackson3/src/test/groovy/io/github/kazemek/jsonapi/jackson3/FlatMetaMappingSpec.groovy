package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.PatchPresence
import io.github.kazemek.jsonapi.jackson.StructuredMember
import io.github.kazemek.jsonapi.jackson.StructuredMemberState
import io.github.kazemek.jsonapi.jackson.StructuredPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.DuplicateMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.DuplicateRelationshipMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.ListMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.NestedPresenceMetaPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.NoMetaPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.NoRelMetaPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.ObjectMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.RenamedRelationshipMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.ScalarMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.ScalarMetaPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.UnmappedRelationshipMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.UuidMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.UuidMetaPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.InstantMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.UriMetaArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithBoxMeta
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithBoxMetaPatch
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithNullEmptyCityMeta
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithTypedContactMeta
import io.github.kazemek.jsonapi.jackson3.testmodel.EmailContact
import io.github.kazemek.jsonapi.jackson3.testmodel.MetaBox
import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.testfixtures.JsonApiFixtures
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleMeta
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleMetaPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithMapMeta
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithMapMetaPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithMeta
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithMetaPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithOptionalMeta
import io.github.kazemek.jsonapi.testfixtures.domainpatch.ArticleWithOptionalMetaPatch
import io.github.kazemek.jsonapi.testfixtures.domainpatch.AuthorMeta
import io.github.kazemek.jsonapi.testfixtures.domainread.FlatMetaArticle
import java.util.Map
import java.util.Optional
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class FlatMetaMappingSpec extends Specification {

  private static final String RESOURCE_WITH_META_AND_REL_META =
  '{"data":{"type":"articles","id":"1",' +
  '"attributes":{"title":"T"},' +
  '"meta":{"source":"cms","note":"n"},' +
  '"relationships":{"author":{"data":{"type":"people","id":"p1"},"meta":{"displayName":"Alice"}}}}}'

  def mapper() {
    JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
  }

  def binder() {
    JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
  }

  def patchReader() {
    JsonApiJackson3.patchReader(JsonMapper.builder().build())
  }

  def patchDtoReader() {
    JsonApiJackson3.patchDtoReader(JsonMapper.builder().build())
  }

  // ============================== WRITE ==============================

  def "write maps resource meta and relationship meta"() {
    given:
    def article = new ArticleWithMeta(
        "1", "T", ResourceIdentifier.of("people", "p1"),
        new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))

    when:
    def resource = mapper().toResource(article)

    then:
    resource.meta().members() == [source: "cms", note: "n"]
    def relationship = resource.relationships().relationships().get("author")
    relationship.meta().members() == [displayName: "Alice"]
    relationship.data() == new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "p1"))
  }

  def "write with null meta properties omits meta"() {
    given:
    def article = new ArticleWithMeta("1", "T", null, null, null)

    when:
    def resource = mapper().toResource(article)

    then:
    resource.meta() == null
    def relationship = resource.relationships().relationships().get("author")
    relationship.meta() == null
    relationship.data() == RelationshipData.NullLinkage.INSTANCE
  }

  def "write with empty meta object emits empty members"() {
    given:
    def article = new ArticleWithMapMeta("1", "T", null, [:], null)

    when:
    def resource = mapper().toResource(article)

    then:
    resource.meta().members().isEmpty()
  }

  def "write with renamed relationship meta uses the wire name"() {
    given:
    def article = new RenamedRelationshipMetaArticle(
        "1", "T", ResourceIdentifier.of("people", "p1"),
        new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))

    when:
    def resource = mapper().toResource(article)

    then:
    resource.relationships().relationships().get("author").meta().members() == [displayName: "Alice"]
  }

  def "write with scalar runtime value in Object meta target fails with INVALID_META_TARGET"() {
    given:
    def article = new ObjectMetaArticle("1", "scalar")

    when:
    mapper().toResource(article)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "write with invalid meta member name surfaces INVALID_META_TARGET"() {
    given:
    def article = new ArticleWithMapMeta("1", "T", null, ["": "bad"], null)

    when:
    mapper().toResource(article)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
    e.propertyPath == "/meta"
  }

  def "write relationship meta failure reports the relationship meta pointer"() {
    given:
    def article = new ArticleWithMapMeta("1", "T", null, null, ["": "bad"])

    when:
    mapper().toResource(article)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
    e.propertyPath == "/relationships/author/meta"
  }

  // ============================== READ ==============================

  def "read binds resource meta and relationship meta"() {
    given:
    def document = documentFrom(RESOURCE_WITH_META_AND_REL_META)

    when:
    def bound = binder().fromResource(
        ((DocumentData.SingleResource) document.data()).resource(), FlatMetaArticle)

    then:
    bound == new FlatMetaArticle(
        "1", "T", ResourceIdentifier.of("people", "p1"),
        new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))
  }

  def "read binds meta-only relationship meta"() {
    given:
    def resource = new ResourceObject(
        "articles", "1", null, null,
        io.github.kazemek.jsonapi.core.model.Relationships.ofRelationships(
        [author: Relationship.metaOnly(Meta.of([displayName: "Alice"]))]),
        null, null, [:])

    when:
    def bound = binder().fromResource(resource, FlatMetaArticle)

    then:
    bound.authorMeta() == new AuthorMeta("Alice")
    bound.author() == null
  }

  def "read absent meta leaves meta properties null"() {
    given:
    def document = documentFrom('{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}')

    when:
    def bound = binder().fromResource(
        ((DocumentData.SingleResource) document.data()).resource(), FlatMetaArticle)

    then:
    bound.meta() == null
    bound.authorMeta() == null
  }

  def "read empty meta object binds empty map meta"() {
    given:
    def document = documentFrom('{"data":{"type":"articles","id":"1","meta":{}}}')

    when:
    def bound = binder().fromResource(
        ((DocumentData.SingleResource) document.data()).resource(), ArticleWithMapMeta)

    then:
    bound.meta().isEmpty()
  }

  // ============================== WRITE/READ ROUND TRIP ==============================

  def "bean meta round-trips through write then read"() {
    given:
    def writer = mapper()
    def binder = binder()
    def article = new ArticleWithMeta(
        "1", "T", ResourceIdentifier.of("people", "p1"),
        new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))
    def document = writer.toDocument(article)

    when:
    def resource = ((DocumentData.SingleResource) document.data()).resource()
    def bound = binder.fromResource(resource, ArticleWithMeta)

    then:
    bound == article
  }

  // ============================== LOW-LEVEL PATCH ==============================

  def "low-level patch binds structured resource meta and relationship meta with ordering"() {
    given:
    def command = patchReader().readValue(RESOURCE_WITH_META_AND_REL_META, ArticleWithMeta)

    expect:
    command.identity() == "1"
    command.changes() == [
      new PatchChange.ResourceMetaChange(
      "meta", "meta",
      new StructuredPatch([
        new StructuredMember("source", "source", new StructuredMemberState.Atomic("cms")),
        new StructuredMember("note", "note", new StructuredMemberState.Atomic("n"))
      ])),
      new PatchChange.AttributeChange("title", "title", "T"),
      new PatchChange.RelationshipChange("author", "author", ResourceIdentifier.of("people", "p1")),
      new PatchChange.RelationshipMetaChange(
      "author", "authorMeta",
      new StructuredPatch([
        new StructuredMember("displayName", "displayName", new StructuredMemberState.Atomic("Alice"))
      ]))
    ]
  }

  def "low-level patch treats map meta atomically"() {
    given:
    def json =
        '{"data":{"type":"articles","id":"1","meta":{"source":"cms"}}}'

    when:
    def command = patchReader().readValue(json, ArticleWithMapMeta)

    then:
    command.changes() == [
      new PatchChange.ResourceMetaChange("meta", "meta", [source: "cms"])
    ]
  }

  def "low-level patch skips supplied meta when no meta property is mapped"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"},"meta":{"source":"cms"}}}'

    when:
    def command = patchReader().readValue(json, io.github.kazemek.jsonapi.testfixtures.domainread.FlatArticle)

    then:
    command.changes() == [
      new PatchChange.AttributeChange("title", "title", "T")
    ]
  }

  def "low-level patch converts Optional-wrapped meta as structured patch"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","meta":{"source":"cms","note":"n"}}}'

    when:
    def command = patchReader().readValue(json, ArticleWithOptionalMeta)

    then:
    command.changes() == [
      new PatchChange.ResourceMetaChange(
      "meta", "meta",
      new StructuredPatch([
        new StructuredMember("source", "source", new StructuredMemberState.Atomic("cms")),
        new StructuredMember("note", "note", new StructuredMemberState.Atomic("n"))
      ]))
    ]
  }

  def "low-level patch atomic meta conversion failure reports the /meta pointer"() {
    given:
    // A meta bean whose source member is a String target while the wire supplies an object
    def json = '{"data":{"type":"articles","id":"1","meta":{"source":{"a":1}}}}'

    when:
    patchReader().readValue(json, ArticleWithMeta)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.UNSUPPORTED_ATTRIBUTE_VALUE
    e.propertyPath == "/meta/source"
  }

  // ============================== TYPED PATCH ==============================

  def "typed patch binds recursive resource meta and relationship meta"() {
    given:
    def patch = patchDtoReader().readValue(RESOURCE_WITH_META_AND_REL_META, ArticleWithMetaPatch)

    expect:
    patch.id() == "1"
    patch.title() == PatchPresence.present("T")
    patch.meta() == PatchPresence.present(
        new ArticleMetaPatch(PatchPresence.present("cms"), PatchPresence.present("n")))
    patch.author() == PatchPresence.present(ResourceIdentifier.of("people", "p1"))
    patch.authorMeta() == PatchPresence.present(new AuthorMeta("Alice"))
  }

  def "typed patch empty meta object binds present shape with omitted members"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","meta":{}}}'

    when:
    def patch = patchDtoReader().readValue(json, ArticleWithMetaPatch)

    then:
    patch.meta() == PatchPresence.present(
        new ArticleMetaPatch(PatchPresence.omitted(), PatchPresence.omitted()))
  }

  def "typed patch absent meta binds omitted"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"}}}'

    when:
    def patch = patchDtoReader().readValue(json, ArticleWithMetaPatch)

    then:
    patch.meta() == PatchPresence.omitted()
    patch.authorMeta() == PatchPresence.omitted()
  }

  def "typed patch atomic map meta"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","meta":{"source":"cms"}}}'

    when:
    def patch = patchDtoReader().readValue(json, ArticleWithMapMetaPatch)

    then:
    patch.meta() == PatchPresence.present([source: "cms"])
  }

  def "typed patch optional-wrapped meta"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","meta":{"source":"cms","note":"n"}}}'

    when:
    def patch = patchDtoReader().readValue(json, ArticleWithOptionalMetaPatch)

    then:
    patch.meta() == PatchPresence.present(Optional.of(new ArticleMeta("cms", "n")))
  }

  def "typed patch rejects supplied resource meta without a mapped meta member"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","attributes":{"title":"T"},"meta":{"source":"cms"}}}'

    when:
    patchDtoReader().readValue(json, NoMetaPatch)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.UNKNOWN_PATCH_MEMBER
    e.propertyPath == "/meta"
  }

  def "typed patch rejects supplied relationship meta without a mapped relationship meta member"() {
    given:
    def json =
        '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":{"type":"people","id":"p1"},"meta":{"displayName":"Alice"}}}}}'

    when:
    patchDtoReader().readValue(json, NoRelMetaPatch)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.UNKNOWN_PATCH_MEMBER
    e.propertyPath == "/relationships/author/meta"
  }

  def "typed patch rejects scalar meta target declaration"() {
    when:
    patchDtoReader().readValue(
        '{"data":{"type":"articles","id":"1","meta":{"source":"cms"}}}', ScalarMetaPatch)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "typed patch rejects nested PatchPresence meta wrapper chain"() {
    when:
    patchDtoReader().readValue(
        '{"data":{"type":"articles","id":"1","meta":{"source":"cms"}}}', NestedPresenceMetaPatch)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  // ============================== INVALID DECLARATIONS ==============================

  def "duplicate resource meta properties are rejected at mapping resolution"() {
    when:
    mapper().toResource(new DuplicateMetaArticle("1", "a", "b"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.DUPLICATE_ROLE
  }

  def "duplicate relationship meta properties for one target are rejected at mapping resolution"() {
    when:
    mapper().toResource(new DuplicateRelationshipMetaArticle(
        "1", ResourceIdentifier.of("people", "p1"), "a", "b"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.DUPLICATE_ROLE
  }

  def "relationship meta referencing an unmapped relationship is rejected at mapping resolution"() {
    when:
    mapper().toResource(new UnmappedRelationshipMetaArticle("1", "x"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.UNRESOLVED_RELATIONSHIP_META
  }

  def "scalar meta target is rejected on the write path"() {
    when:
    mapper().toResource(new ScalarMetaArticle("1", "x"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "list meta target is rejected on the write path"() {
    when:
    mapper().toResource(new ListMetaArticle("1", ["x"]))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "scalar meta target is rejected on the read path"() {
    given:
    def document = documentFrom('{"data":{"type":"articles","id":"1"}}')

    when:
    binder().fromResource(((DocumentData.SingleResource) document.data()).resource(), ScalarMetaArticle)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "scalar meta target is rejected on the low-level patch path"() {
    when:
    patchReader().readValue(
        '{"data":{"type":"articles","id":"1"}}', ScalarMetaArticle)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  // ============================== SCALAR META TARGET REJECTION (Jackson-aware) ==============

  def "UUID whole-meta target is rejected on the write path even when meta is absent"() {
    when:
    mapper().toResource(new UuidMetaArticle("1", null))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "UUID whole-meta target is rejected on the read path even when meta is absent"() {
    given:
    def document = documentFrom('{"data":{"type":"articles","id":"1"}}')

    when:
    binder().fromResource(((DocumentData.SingleResource) document.data()).resource(), UuidMetaArticle)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "UUID whole-meta target is rejected on the low-level patch path even when meta is absent"() {
    when:
    patchReader().readValue('{"data":{"type":"articles","id":"1"}}', UuidMetaArticle)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "UUID typed PATCH meta target is rejected on the typed patch path"() {
    when:
    patchDtoReader().readValue(
        '{"data":{"type":"articles","id":"1"}}', UuidMetaPatch)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "java.time whole-meta target is rejected on the write path even when meta is absent"() {
    when:
    mapper().toResource(new InstantMetaArticle("1", null))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "java.time whole-meta target is rejected on the read path even when meta is absent"() {
    given:
    def document = documentFrom('{"data":{"type":"articles","id":"1"}}')

    when:
    binder().fromResource(((DocumentData.SingleResource) document.data()).resource(), InstantMetaArticle)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "URI whole-meta target is rejected on the write path even when meta is absent"() {
    when:
    mapper().toResource(new UriMetaArticle("1", null))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  def "URI whole-meta target is rejected on the low-level patch path even when meta is absent"() {
    when:
    patchReader().readValue('{"data":{"type":"articles","id":"1"}}', UriMetaArticle)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
  }

  // ============================== POSITIVE WHOLE-META TARGET SHAPES =======================

  def "Object whole-meta target is a valid declaration and writes a map value"() {
    given:
    def article = new ObjectMetaArticle("1", [source: "cms"])

    when:
    def resource = mapper().toResource(article)

    then:
    resource.meta().members() == [source: "cms"]
  }

  def "Map whole-meta target round-trips"() {
    given:
    def article = new ArticleWithMapMeta("1", "T", null, [source: "cms"], [displayName: "Alice"])
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, ArticleWithMapMeta)

    expect:
    bound.meta() == [source: "cms"]
    bound.authorMeta() == [displayName: "Alice"]
  }

  def "Optional-wrapped bean meta target is valid on write and read"() {
    given:
    def article = new ArticleWithOptionalMeta(
        "1", "T", null, Optional.of(new ArticleMeta("cms", "n")), Optional.of(new AuthorMeta("Alice")))
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, ArticleWithOptionalMeta)

    expect:
    bound.meta() == Optional.of(new ArticleMeta("cms", "n"))
    bound.authorMeta() == Optional.of(new AuthorMeta("Alice"))
  }

  def "typed PatchPresence bean/map/object/optional targets are valid declarations"() {
    expect:
    // presence-aware bean target binds through the typed reader without declaration failure
    !patchDtoReader().readValue(
        '{"data":{"type":"articles","id":"1","meta":{"source":"cms"}}}', ArticleWithMetaPatch)
        .meta().isOmitted()
    patchDtoReader().readValue(
        '{"data":{"type":"articles","id":"1","meta":{"source":"cms"}}}', ArticleWithMapMetaPatch)
        .meta() == PatchPresence.present([source: "cms"])
    patchDtoReader().readValue(
        '{"data":{"type":"articles","id":"1","meta":{"source":"cms"}}}', ArticleWithOptionalMetaPatch)
        .meta() == PatchPresence.present(Optional.of(new ArticleMeta("cms", null)))
  }

  // ============================== SPARSE FIELDSETS ==============================

  def "resource meta is emitted even for an empty fieldset selection"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithMeta(
        "1", "T", ResourceIdentifier.of("people", "p1"),
        new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))
    def context = io.github.kazemek.jsonapi.jackson.CompoundSerializationContext.defaults()
        .withFieldsets([articles: []])
        .withFieldPolicy(io.github.kazemek.jsonapi.jackson.FieldPolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    def resource = ((DocumentData.SingleResource) mapped.document().data()).resource()
    resource.attributes() == null
    resource.relationships() == null
    resource.meta().members() == [source: "cms", note: "n"]
  }

  def "relationship meta is absent when its relationship is fieldset-excluded"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithMeta(
        "1", "T", ResourceIdentifier.of("people", "p1"),
        new ArticleMeta("cms", "n"), new AuthorMeta("Alice"))
    def context = io.github.kazemek.jsonapi.jackson.CompoundSerializationContext.defaults()
        .withFieldsets([articles: ["title"]])
        .withFieldPolicy(io.github.kazemek.jsonapi.jackson.FieldPolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    def resource = ((DocumentData.SingleResource) mapped.document().data()).resource()
    resource.relationships() == null
    resource.meta().members() == [source: "cms", note: "n"]
  }

  def "meta is not a valid sparse-fieldset field name"() {
    given:
    def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def article = new ArticleWithMeta("1", "T", null, new ArticleMeta("cms", "n"), null)
    def context = io.github.kazemek.jsonapi.jackson.CompoundSerializationContext.defaults()
        .withFieldsets([articles: ["meta"]])
        .withFieldPolicy(io.github.kazemek.jsonapi.jackson.FieldPolicy.allowAll())

    when:
    mapper.toMappedDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_FIELDSET_FIELD
  }

  // ============================== WIRE-LEVEL INVALID META NULL ==============================

  def "wire-level resource meta null is rejected at the reader"() {
    when:
    documentFrom('{"data":{"type":"articles","id":"1","meta":null}}')

    then:
    def e = thrown(io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException)
    e.category == io.github.kazemek.jsonapi.jackson.CodecFailureCategory.UNEXPECTED_TOKEN
  }

  def "wire-level relationship meta null is rejected at the reader"() {
    when:
    documentFrom(
        '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":{"type":"people","id":"p1"},"meta":null}}}}')

    then:
    def e = thrown(io.github.kazemek.jsonapi.jackson.JsonApiDocumentReadException)
    e.category == io.github.kazemek.jsonapi.jackson.CodecFailureCategory.UNEXPECTED_TOKEN
  }

  // ============================== JACKSON AUTHORITY AT META LOCATIONS ======================

  def "low-level recursive meta preserves generic JavaType binding"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","meta":{"value":"42"}}}'

    when:
    def command = patchReader().readValue(json, ArticleWithBoxMeta)

    then: // the nested value converts as Integer, not Object/String/raw type
    command.changes() == [
      new PatchChange.ResourceMetaChange(
      "meta", "meta",
      new StructuredPatch(
      [
        new StructuredMember("value", "value", new StructuredMemberState.Atomic(42))
      ]))
    ]
  }

  def "typed meta preserves generic JavaType binding"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","meta":{"value":"42"}}}'

    when:
    def patch = patchDtoReader().readValue(json, ArticleWithBoxMetaPatch)

    then:
    patch.meta() == PatchPresence.present(new MetaBox<>(42))
  }

  def "low-level recursive meta routes nested explicit null through the property null provider"() {
    given:
    def json = '{"data":{"type":"articles","id":"1","meta":{"city":null}}}'

    when: // the city setter's @JsonSetter(nulls = Nulls.AS_EMPTY) null provider yields "" for null
    def command = patchReader().readValue(json, ArticleWithNullEmptyCityMeta)

    then: // not the root String deserializer's null value (null)
    command.changes() == [
      new PatchChange.ResourceMetaChange(
      "meta", "meta",
      new StructuredPatch(
      [
        new StructuredMember("city", "city", new StructuredMemberState.Atomic(""))
      ]))
    ]
  }

  def "low-level recursive meta preserves a property-level TypeDeserializer for a polymorphic nested member"() {
    given:
    def json =
        '{"data":{"type":"articles","id":"1","meta":{"contact":{"kind":"email","email":"a@b.c"}}}}'

    when: // the polymorphic contact converts through the property's TypeDeserializer path
    def command = patchReader().readValue(json, ArticleWithTypedContactMeta)

    then:
    command.changes() == [
      new PatchChange.ResourceMetaChange(
      "meta", "meta",
      new StructuredPatch(
      [
        new StructuredMember(
        "contact",
        "contact",
        new StructuredMemberState.Atomic(new EmailContact("a@b.c")))
      ]))
    ]
  }

  // ============================== FROM-DOCUMENT DATA-LESS RELATIONSHIP META ================

  def "fromDocument skips a data-less relationship with meta on the low-level path"() {
    given:
    def document = dataLessMetaDocument(null)
    def command = patchReader().fromDocument(document, ArticleWithMeta)

    expect:
    command.identity() == "1"
    command.changes().isEmpty()
  }

  def "fromDocument skips a data-less relationship with meta but keeps other supplied changes on the low-level path"() {
    given:
    def document = dataLessMetaDocument([title: "T"])
    def command = patchReader().fromDocument(document, ArticleWithMeta)

    expect:
    command.changes() == [
      new PatchChange.AttributeChange("title", "title", "T")
    ]
  }

  def "fromDocument binds a data-less relationship with meta as Omitted on the typed path"() {
    given:
    def document = dataLessMetaDocument(null)
    def dto = patchDtoReader().fromDocument(document, ArticleWithMetaPatch)

    expect:
    dto.author().isOmitted()
    dto.authorMeta().isOmitted()
  }

  def "fromDocument binds a data-less relationship with meta as Omitted but keeps other supplied members on the typed path"() {
    given:
    def document = dataLessMetaDocument([title: "T"])
    def dto = patchDtoReader().fromDocument(document, ArticleWithMetaPatch)

    expect:
    dto.title() == PatchPresence.present("T")
    dto.author().isOmitted()
    dto.authorMeta().isOmitted()
  }

  private static JsonApiDocument dataLessMetaDocument(Map suppliedAttrs) {
    def resource = new ResourceObject(
        "articles",
        "1",
        null,
        suppliedAttrs == null ? null : Attributes.ofAttributes(suppliedAttrs),
        Relationships.ofRelationships(
        [author: Relationship.metaOnly(Meta.of([displayName: "Alice"]))]),
        null,
        null,
        [:])
    return new JsonApiDocument(
        new DocumentData.SingleResource(resource), null, null, null, null, null, [:])
  }

  private static JsonApiDocument documentFrom(String json) {
    JsonApiJackson3.reader(JsonMapper.builder().build(),
        io.github.kazemek.jsonapi.jackson.DocumentReadContext.resourceDefaults()).readValue(json)
  }
}
