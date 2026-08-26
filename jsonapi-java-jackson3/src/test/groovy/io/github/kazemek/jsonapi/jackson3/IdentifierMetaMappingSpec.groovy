package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.ArrayIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.BeanOnToManyIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.DuplicateIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.EmptyNameIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.EncodedIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.GenericIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.IdMetaBox
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.LengthMismatchIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.ListOnToOneIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.MapIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.RenamedRelationshipIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.ScalarIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SerializedIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SetIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SnakeIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SnakeIdentifierMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.UnmappedIdentifierMetaArticle
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleWithIdentifierMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatIdentifierMetaArticle
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatMetaArticle
import spock.lang.Specification
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

  def "write overlays to-one identifier meta without changing relationship meta"() {
    given:
    def article = new ArticleWithIdentifierMeta(
        "1",
        "T",
        ResourceIdentifier.of("people", "p1"),
        List.of(),
        new AuthorMeta("Alice"),
        new AuthorIdMeta("editor"),
        null)

    when:
    def resource = mapper().toResource(article)

    then:
    def relationship = resource.relationships().relationships().get("author")
    relationship.meta().members() == [displayName: "Alice"]
    relationship.data() == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]))
  }

  def "to-one identifier meta with null linkage fails"() {
    given:
    def article = new ArticleWithIdentifierMeta(
        "1", "T", null, List.of(), null, new AuthorIdMeta("editor"), null)

    when:
    mapper().toResource(article)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.propertyPath() == "/relationships/author/data/meta"
  }

  def "to-many identifier meta length mismatch fails at the linkage list"() {
    given:
    def article = new LengthMismatchIdentifierMetaArticle(
        "1",
        List.of(ResourceIdentifier.of("comments", "c1")),
        List.of(new CommentIdMeta(true), new CommentIdMeta(false)))

    when:
    mapper().toResource(article)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.propertyPath() == "/relationships/comments/data"
  }

  def "array to-many identifier meta writes and reads aligned with linkage"() {
    given:
    def article = new ArrayIdentifierMetaArticle(
        "1",
        List.of(ResourceIdentifier.of("comments", "c1"), ResourceIdentifier.of("comments", "c2")),
        [new CommentIdMeta(true), null] as CommentIdMeta[])

    when:
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, ArrayIdentifierMetaArticle)

    then:
    def comments = ((RelationshipData.IdentifierCollectionLinkage) resource.relationships()
        .relationships().get("comments").data()).identifiers()
    comments[0].meta().members() == [pinned: true]
    comments[1].meta() == null
    bound.commentIdMetas()[0].pinned() == true
    bound.commentIdMetas()[1] == null
  }

  def "generic JavaType identifier meta is preserved on write and read"() {
    given:
    def article = new GenericIdentifierMetaArticle(
        "1", ResourceIdentifier.of("people", "p1"), new IdMetaBox<Integer>(7))

    when:
    def resource = mapper().toResource(article)
    def bound = binder().fromResource(resource, GenericIdentifierMetaArticle)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([value: 7]), [:]))
    bound.authorIdMeta() == new IdMetaBox<Integer>(7)
  }

  def "configured Jackson naming applies to identifier meta members"() {
    given:
    def snakeMapper = JsonApiJackson3.resourceMapper(
        JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build())
    def article = new SnakeIdentifierMeta(
        "1", ResourceIdentifier.of("people", "p1"), new SnakeIdMeta("editor"))

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
        "1", ResourceIdentifier.of("people", "p1"), new AuthorIdMeta("editor"))

    when:
    def resource = mapper().toResource(article)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]))
  }

  def "property-scoped serializer encodes identifier meta"() {
    given:
    def article = new SerializedIdentifierMetaArticle(
        "1", ResourceIdentifier.of("people", "p1"), new EncodedIdMeta("editor"))

    when:
    def resource = mapper().toResource(article)

    then:
    def authorMembers = ((RelationshipData.SingleLinkage) resource.relationships()
        .relationships().get("author").data()).identifier().meta().members()
    authorMembers == [encoded: "editor"]
  }

  def "KAZ-77 resource and relationship meta remain distinct from identifier meta on read"() {
    given:
    def resource = new ResourceObject(
        "articles",
        "1",
        null,
        Attributes.ofAttributes([title: "T"]),
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.SingleLinkage(
          new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:])),
          null,
          Meta.of([displayName: "Alice"]),
          [:])]),
        null,
        Meta.of([source: "cms", note: "n"]),
        [:])

    when:
    def bound = binder().fromResource(resource, FlatMetaArticle)

    then:
    bound == new FlatMetaArticle(
        "1",
        "T",
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]),
        new io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.ArticleMeta("cms", "n"),
        new AuthorMeta("Alice"))
    bound.authorMeta().displayName() == "Alice"
  }

  def "ordinary FlatIdentifierMetaArticle linkage is unchanged when identifier meta is absent"() {
    given:
    def resource = new ResourceObject(
        "articles",
        "1",
        null,
        Attributes.ofAttributes([title: "T"]),
        Relationships.ofRelationships(
        [author: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "p1")))]),
        null,
        null,
        [:])

    when:
    def bound = binder().fromResource(resource, FlatIdentifierMetaArticle)

    then:
    bound.author() == ResourceIdentifier.of("people", "p1")
    bound.authorIdMeta() == null
    bound.authorMeta() == null
  }

  def "low-level PATCH does not emit an identifier-meta change variant"() {
    when:
    def command = patchReader().readValue(
        '{"data":{"type":"articles","id":"1","relationships":{"author":{"data":{"type":"people","id":"p1","meta":{"role":"editor"}}}}}}',
        ArticleWithIdentifierMeta)

    then:
    command.changes().size() == 1
    command.changes()[0] instanceof PatchChange.RelationshipChange
    command.changes().every { !(it instanceof PatchChange.ResourceMetaChange) }
    command.changes().every { !(it instanceof PatchChange.RelationshipMetaChange) }
    def linkage = ((PatchChange.RelationshipChange) command.changes()[0]).value()
    linkage == new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:])
  }

  def "duplicate identifier meta properties are rejected"() {
    when:
    mapper().toResource(new DuplicateIdentifierMetaArticle(
        "1", ResourceIdentifier.of("people", "p1"), new AuthorIdMeta("a"), new AuthorIdMeta("b")))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.DUPLICATE_ROLE
    e.propertyPath() == "/relationships/author/data/meta"
  }

  def "identifier meta referencing an unmapped relationship is rejected"() {
    when:
    mapper().toResource(new UnmappedIdentifierMetaArticle("1", new AuthorIdMeta("x")))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.UNRESOLVED_IDENTIFIER_META
    e.propertyPath() == "/relationships/nonexistent/data/meta"
  }

  def "scalar to-one identifier meta is rejected"() {
    when:
    mapper().toResource(new ScalarIdentifierMetaArticle(
        "1", ResourceIdentifier.of("people", "p1"), "editor"))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.propertyPath() == "/relationships/author/data/meta"
  }

  def "Set to-many identifier meta is rejected"() {
    when:
    mapper().toResource(new SetIdentifierMetaArticle(
        "1",
        List.of(ResourceIdentifier.of("comments", "c1")),
        Set.of(new CommentIdMeta(true))))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.propertyPath() == "/relationships/comments/data/meta"
  }

  def "Map to-many identifier meta is rejected"() {
    when:
    mapper().toResource(new MapIdentifierMetaArticle(
        "1",
        List.of(ResourceIdentifier.of("comments", "c1")),
        [c1: new CommentIdMeta(true)]))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.propertyPath() == "/relationships/comments/data/meta"
  }

  def "List identifier meta on a to-one relationship is rejected"() {
    when:
    mapper().toResource(new ListOnToOneIdentifierMetaArticle(
        "1", ResourceIdentifier.of("people", "p1"), List.of(new AuthorIdMeta("editor"))))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.propertyPath() == "/relationships/author/data/meta"
  }

  def "bean identifier meta on a to-many relationship is rejected"() {
    when:
    mapper().toResource(new BeanOnToManyIdentifierMetaArticle(
        "1", List.of(ResourceIdentifier.of("comments", "c1")), new CommentIdMeta(true)))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.propertyPath() == "/relationships/comments/data/meta"
  }

  def "empty identifier meta value is rejected as an invalid member name"() {
    when:
    mapper().toResource(new EmptyNameIdentifierMetaArticle(
        "1", ResourceIdentifier.of("people", "p1"), new AuthorIdMeta("editor")))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic == MappingDiagnostic.INVALID_IDENTIFIER_META_TARGET
    e.location() == null
  }
}
