package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext
import io.github.kazemek.jsonapi.jackson.IdentifierConverter
import io.github.kazemek.jsonapi.jackson.IncludePath
import io.github.kazemek.jsonapi.jackson.IncludePolicy
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson.RelationshipLinkage
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.ArrayIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.EncodedIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.GenericIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.IdMetaBox
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.MapMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.NonEmittingIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.OptionalIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.RenamedRelationshipIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.ScalarIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.ScalarSerializedMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SerializedIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SetIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SilentIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SnakeIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SnakeIdentifierMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.WrappedDomainArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.WrappedMappedArticle
import io.github.kazemek.jsonapi.jackson3.LinkageMapperFixtures.FlatAuthor
import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithArray
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithOptional
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithSet
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Comment
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person
import java.util.Optional
import java.util.Set
import spock.lang.Specification
import tools.jackson.databind.JavaType
import tools.jackson.databind.PropertyNamingStrategies
import tools.jackson.databind.json.JsonMapper

class IdentifierMetaMappingSpec extends Specification {

  static def mapper() {
    JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
  }

  static def binder() {
    JsonApiJackson3.resourceBinder(JsonMapper.builder().build())
  }

  static def patchReader() {
    JsonApiJackson3.patchReader(JsonMapper.builder().build())
  }

  def "array to-many RelationshipLinkage writes and reads per-element meta"() {
    given:
    def article = new ArrayIdentifierMetaArticle(
        "1",
        [
          new RelationshipLinkage(ResourceIdentifier.of("comments", "c1"), new CommentIdMeta(true)),
          new RelationshipLinkage(ResourceIdentifier.of("comments", "c2"), null)
        ] as RelationshipLinkage[])

    when:
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, ArrayIdentifierMetaArticle)

    then:
    def comments = ((RelationshipData.IdentifierCollectionLinkage) resource.relationships()
        .relationships().get("comments").data()).identifiers()
    comments[0].meta().members() == [pinned: true]
    comments[1].meta() == null
    bound.comments()[0].target() == new ResourceIdentifier("comments", "c1", null, Meta.of([pinned: true]), [:])
    bound.comments()[0].meta().pinned() == true
    bound.comments()[1].meta() == null
  }

  def "Set of RelationshipLinkage writes and reads without a sibling sequence"() {
    given:
    def article = new SetIdentifierMetaArticle(
        "1",
        Set.of(new RelationshipLinkage(ResourceIdentifier.of("comments", "c1"), new CommentIdMeta(true))))

    when:
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, SetIdentifierMetaArticle)

    then:
    def comments = ((RelationshipData.IdentifierCollectionLinkage) resource.relationships()
        .relationships().get("comments").data()).identifiers()
    comments.size() == 1
    comments[0].meta().members() == [pinned: true]
    bound.comments().size() == 1
    bound.comments().iterator().next().meta().pinned() == true
  }

  def "Optional RelationshipLinkage writes and reads identifier meta"() {
    given:
    def article = new OptionalIdentifierMetaArticle(
        "1",
        Optional.of(new RelationshipLinkage(ResourceIdentifier.of("people", "p1"), new AuthorIdMeta("editor"))))

    when:
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, OptionalIdentifierMetaArticle)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]))
    bound.author().get().meta() == new AuthorIdMeta("editor")
  }

  def "generic JavaType identifier meta is preserved on write and read"() {
    given:
    def article = new GenericIdentifierMetaArticle(
        "1",
        new RelationshipLinkage(ResourceIdentifier.of("people", "p1"), new IdMetaBox<Integer>(7)))

    when:
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, GenericIdentifierMetaArticle)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([value: 7]), [:]))
    bound.author().meta() == new IdMetaBox<Integer>(7)
  }

  def "Map identifier meta preserves generic JavaType on a to-many wrapper"() {
    given:
    def article = new MapMetaArticle(
        "1",
        List.of(new RelationshipLinkage(ResourceIdentifier.of("comments", "c1"), Map.of("pinned", true))))

    when:
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, MapMetaArticle)

    then:
    def comments = ((RelationshipData.IdentifierCollectionLinkage) resource.relationships()
        .relationships().get("comments").data()).identifiers()
    comments[0].meta().members() == [pinned: true]
    bound.comments()[0].meta() == [pinned: true]
  }

  def "configured Jackson naming applies to identifier meta members"() {
    given:
    def snakeMapper = JsonApiJackson3.resourceMapper(
        JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build())
    def article = new SnakeIdentifierMeta(
        "1", new RelationshipLinkage(ResourceIdentifier.of("people", "p1"), new SnakeIdMeta("editor")))

    when:
    def resource = snakeMapper.toResource(article)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([display_role: "editor"]), [:]))
  }

  def "renamed relationship identifier meta uses the wire name"() {
    given:
    def article = new RenamedRelationshipIdentifierMetaArticle(
        "1", new RelationshipLinkage(ResourceIdentifier.of("people", "p1"), new AuthorIdMeta("editor")))

    when:
    def resource = mapper().toResource(article)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]))
  }

  def "type-level serializer encodes identifier meta"() {
    given:
    def article = new SerializedIdentifierMetaArticle(
        "1", new RelationshipLinkage(ResourceIdentifier.of("people", "p1"), new EncodedIdMeta("editor")))

    when:
    def resource = mapper().toResource(article)

    then:
    def authorMembers = ((RelationshipData.SingleLinkage) resource.relationships()
        .relationships().get("author").data()).identifier().meta().members()
    authorMembers == [encoded: "editor"]
  }

  def "serializer non-emission leaves existing to-one identifier meta in place"() {
    given:
    def article = new NonEmittingIdentifierMetaArticle(
        "1",
        new RelationshipLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]),
        new SilentIdMeta("ignored")))

    when:
    def resource = mapper().toResource(article)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]))
  }

  def "additional members survive identifier-meta overlay"() {
    given:
    def extra = ["ext:href": "https://example.test/p1"]
    def article = new GenericIdentifierMetaArticle(
        "1",
        new RelationshipLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "old"]), extra),
        new IdMetaBox<Integer>(7)))

    when:
    def resource = mapper().toResource(article)

    then:
    def identifier = ((RelationshipData.SingleLinkage) resource.relationships()
        .relationships().get("author").data()).identifier()
    identifier.meta().members() == [value: 7]
    identifier.additionalMembers() == extra
  }

  def "null wrapper meta preserves existing identifier meta and additional members"() {
    given:
    def extra = ["ext:href": "https://example.test/p1"]
    def existing = new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), extra)
    def article = new GenericIdentifierMetaArticle("1", new RelationshipLinkage(existing, null))

    when:
    def resource = mapper().toResource(article)

    then:
    def identifier = ((RelationshipData.SingleLinkage) resource.relationships()
        .relationships().get("author").data()).identifier()
    identifier.meta().members() == [role: "editor"]
    identifier.additionalMembers() == extra
  }

  def "lid on a wrapped ResourceIdentifier survives overlay"() {
    given:
    def article = new GenericIdentifierMetaArticle(
        "1",
        new RelationshipLinkage(
        new ResourceIdentifier("people", null, "lid-1", null, [:]),
        new IdMetaBox<Integer>(3)))

    when:
    def resource = mapper().toResource(article)

    then:
    def identifier = ((RelationshipData.SingleLinkage) resource.relationships()
        .relationships().get("author").data()).identifier()
    identifier.id() == null
    identifier.lid() == "lid-1"
    identifier.meta().members() == [value: 3]
  }

  def "inclusion walks wrapped Person and Comment targets"() {
    given:
    def article = new WrappedDomainArticle(
        "1",
        new RelationshipLinkage(new Person("p1", "Alice"), new AuthorIdMeta("editor")),
        List.of(new RelationshipLinkage(new Comment("c1", "Hi", null), new CommentIdMeta(true))))
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([
          IncludePath.of("author"),
          IncludePath.of("comments")
        ])
        .withIncludePolicy(IncludePolicy.allowAll())

    when:
    def document = mapper().toDocument(article, null, context)

    then:
    document.included()*.type() as Set == ["people", "comments"] as Set
    document.included()*.id() as Set == ["p1", "c1"] as Set
    def author = ((RelationshipData.SingleLinkage) ((DocumentData.SingleResource) document.data())
        .resource().relationships().relationships().get("author").data()).identifier()
    author.meta().members() == [role: "editor"]
  }

  def "custom linkage mapper receives the unwrapped target type"() {
    given:
    def seenTypes = []
    def linkageMapper = { RelationshipData data, JavaType target ->
      seenTypes.add(target)
      def identifier = ((RelationshipData.SingleLinkage) data).identifier()
      return new FlatAuthor(identifier.type(), identifier.id())
    } as RelationshipLinkageMapper
    def binder = JsonApiJackson3.resourceBinder(
        JsonMapper.builder().build(), IdentifierConverter.defaults(), [(FlatAuthor): linkageMapper])
    def resource = new ResourceObject(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: Relationship.withData(
          new RelationshipData.SingleLinkage(
          new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:])))]),
        null,
        null,
        [:])

    when:
    def bound = binder.fromResource(resource, WrappedMappedArticle)

    then:
    seenTypes.every { it.rawClass == FlatAuthor }
    bound.author().target() == new FlatAuthor("people", "p1")
    bound.author().meta() == new AuthorIdMeta("editor")
  }

  def "converted scalar identifier meta is INVALID_META_TARGET"() {
    given:
    def article = new ScalarSerializedMetaArticle(
        "1", new RelationshipLinkage(ResourceIdentifier.of("people", "p1"), new ScalarIdMeta("editor")))

    when:
    mapper().toResource(article)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_META_TARGET
    e.propertyPath() == "/relationships/author/data/meta"
  }

  def "low-level PATCH preserves identifier meta on array linkage"() {
    when:
    def command = patchReader().readValue(
        TestSupportResources.readCorpusUtf8("patch/comments-identifier-meta.json"),
        FlatArticleWithArray)

    then:
    command.changes().size() == 1
    command.changes()[0] instanceof PatchChange.RelationshipChange
    def comments = (ResourceIdentifier[]) command.changes()[0].value()
    comments[0].meta().members() == [pinned: true]
    comments[1].meta() == null
  }

  def "low-level PATCH preserves identifier meta on Set linkage"() {
    given:
    def json =
        '{"data":{"type":"articles","id":"1","relationships":{"tags":{"data":[{"type":"tags","id":"t1","meta":{"pinned":true}}]}}}}'

    when:
    def command = patchReader().readValue(json, FlatArticleWithSet)

    then:
    command.changes().size() == 1
    command.changes()[0] instanceof PatchChange.RelationshipChange
    def tags = (Set) command.changes()[0].value()
    tags.size() == 1
    ((ResourceIdentifier) tags.iterator().next()).meta().members() == [pinned: true]
  }

  def "low-level PATCH preserves identifier meta on Optional linkage"() {
    when:
    def command = patchReader().readValue(
        TestSupportResources.readCorpusUtf8("patch/author-identifier-meta.json"),
        FlatArticleWithOptional)

    then:
    command.changes().size() == 1
    command.changes()[0] instanceof PatchChange.RelationshipChange
    def author = ((Optional) command.changes()[0].value()).get()
    author == new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:])
  }
}
