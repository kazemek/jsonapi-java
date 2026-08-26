package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.jackson.PatchChange
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.ArrayIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.EncodedIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.GenericIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.IdMetaBox
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.NonEmittingIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.RenamedRelationshipIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SerializedIdentifierMetaArticle
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SnakeIdMeta
import io.github.kazemek.jsonapi.jackson3.IdentifierMetaFixtures.SnakeIdentifierMeta
import io.github.kazemek.jsonapi.testsupport.TestSupportResources
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.AuthorIdMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainpatch.CommentIdMeta
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithArray
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithOptional
import io.github.kazemek.jsonapi.testsupport.fixtures.domainread.FlatArticleWithSet
import java.util.Optional
import java.util.Set
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

  def "serializer non-emission leaves existing to-one identifier meta in place"() {
    given:
    def article = new NonEmittingIdentifierMetaArticle(
        "1",
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]),
        Map.of())

    when:
    def resource = mapper().toResource(article)

    then:
    def authorData = resource.relationships().relationships().get("author").data()
    authorData == new RelationshipData.SingleLinkage(
        new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), [:]))
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
