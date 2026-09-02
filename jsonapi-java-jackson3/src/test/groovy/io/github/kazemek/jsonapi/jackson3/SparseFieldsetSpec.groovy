package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceIdentity
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson.mapping.MappedDocument
import io.github.kazemek.jsonapi.jackson.representation.FieldAllowance
import io.github.kazemek.jsonapi.jackson.representation.FieldPolicy
import io.github.kazemek.jsonapi.jackson.representation.IncludePath
import io.github.kazemek.jsonapi.jackson.representation.IncludePolicy
import io.github.kazemek.jsonapi.jackson.representation.RepresentationPolicy
import io.github.kazemek.jsonapi.jackson.representation.RepresentationSelection
import io.github.kazemek.jsonapi.fixtures.domainwrite.Article
import io.github.kazemek.jsonapi.fixtures.domainpatch.ArticleMeta
import io.github.kazemek.jsonapi.fixtures.domainpatch.ArticleWithMeta
import io.github.kazemek.jsonapi.fixtures.domainwrite.BlogWithJsonProperty
import io.github.kazemek.jsonapi.fixtures.domainwrite.Comment
import io.github.kazemek.jsonapi.fixtures.domainwrite.Person
import io.github.kazemek.jsonapi.fixtures.sparsefieldset.ArticleWithRenamedAuthor
import io.github.kazemek.jsonapi.fixtures.sparsefieldset.AccessCountingFieldsetArticle
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.List
import java.util.Map
import java.util.Set
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import tools.jackson.databind.json.JsonMapper

class SparseFieldsetSpec extends Specification {

  @Shared
  JsonApiResourceMapper mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

  @Unroll
  def "toMappedDocument applies #description"() {
    given:
    def selection = selectionFor(fieldsets)

    when:
    def mapped = mapper.toMappedDocument(article(), null, selection, RepresentationPolicy.defaults())

    then:
    assertFieldsetResource(
        primaryResource(mapped),
        "articles",
        "1",
        expectedAttributeNames,
        expectedAttributes,
        expectedRelationshipNames,
        expectedLinkage)
    mapped.document().included() == null
    mapped.sparseFieldsetLinkageExemptions().isEmpty()

    where:
    description | fieldsets | expectedAttributeNames | expectedAttributes | expectedRelationshipNames | expectedLinkage
    "an absent type fieldset as unrestricted" | [:] | ["title", "body-text"] | ["title": "Title", "body-text": "Body"] | ["comments", "author"] | ["comments": commentsLinkage(), "author": personLinkage(dan())]
    "an attribute-only fieldset" | ["articles": ["title"]] | ["title"] | ["title": "Title"] | null | [:]
    "a relationship-only fieldset" | ["articles": ["author"]] | null | [:] | ["author"] | ["author": personLinkage(dan())]
    "a mixed attribute and relationship fieldset" | ["articles": ["title", "author"]] | ["title"] | ["title": "Title"] | ["author"] | ["author": personLinkage(dan())]
    "a present-empty fieldset as no fields" | ["articles": []] | null | [:] | null | [:]
    "the renamed body-text attribute" | ["articles": ["body-text"]] | ["body-text"] | ["body-text": "Body"] | null | [:]
    "a fieldset in mapping-definition order" | ["articles": [
        "author",
        "body-text",
        "title"
      ]] | ["title", "body-text"] | ["title": "Title", "body-text": "Body"] | ["author"] | ["author": personLinkage(dan())]
  }

  @Unroll
  def "toMappedResourceCollection applies #description"() {
    given:
    def selection = selectionFor(fieldsets)

    when:
    def mapped = mapper.toMappedResourceCollection(
        List.of(article()), null, selection, RepresentationPolicy.defaults())

    then:
    def resources = primaryResources(mapped)
    resources.size() == 1
    assertFieldsetResource(
        resources[0],
        "articles",
        "1",
        expectedAttributeNames,
        expectedAttributes,
        expectedRelationshipNames,
        expectedLinkage)
    mapped.document().included() == null
    mapped.sparseFieldsetLinkageExemptions().isEmpty()

    where:
    description | fieldsets | expectedAttributeNames | expectedAttributes | expectedRelationshipNames | expectedLinkage
    "an attribute-only fieldset" | ["articles": ["title"]] | ["title"] | ["title": "Title"] | null | [:]
    "a relationship-only fieldset" | ["articles": ["comments"]] | null | [:] | ["comments"] | ["comments": commentsLinkage()]
    "a to-one relationship fieldset" | ["articles": ["author"]] | null | [:] | ["author"] | ["author": personLinkage(dan())]
    "a present-empty fieldset" | ["articles": []] | null | [:] | null | [:]
    "a full fieldset" | ["articles": [
        "title",
        "body-text",
        "comments",
        "author"
      ]] | ["title", "body-text"] | ["title": "Title", "body-text": "Body"] | ["comments", "author"] | ["comments": commentsLinkage(), "author": personLinkage(dan())]
  }

  @Unroll
  def "toMappedDocument includes an author with #description"() {
    given:
    def selection = selectionFor(fieldsets, ["author"])
    def policy = RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(article(), null, selection, policy)

    then:
    assertFieldsetResource(
        primaryResource(mapped),
        "articles",
        "1",
        expectedPrimaryAttributeNames,
        expectedPrimaryAttributes,
        expectedPrimaryRelationshipNames,
        expectedPrimaryLinkage)
    assertIncludedResource(
        mapped,
        "people",
        "9",
        expectedIncludedAttributeNames,
        expectedIncludedAttributes,
        null,
        [:])
    mapped.sparseFieldsetLinkageExemptions() == expectedExemptions

    where:
    description | fieldsets | expectedPrimaryAttributeNames | expectedPrimaryAttributes | expectedPrimaryRelationshipNames | expectedPrimaryLinkage | expectedIncludedAttributeNames | expectedIncludedAttributes | expectedExemptions
    "an absent people fieldset" | ["articles": ["title"]] | ["title"] | ["title": "Title"] | null | [:] | ["name"] | ["name": "Dan"] | Set.of(ResourceIdentity.ofId("people", "9"))
    "a present-empty people fieldset" | ["articles": ["title"], "people": []] | ["title"] | ["title": "Title"] | null | [:] | null | [:] | Set.of(ResourceIdentity.ofId("people", "9"))
    "a relationship-only primary fieldset" | ["articles": ["author"]] | null | [:] | ["author"] | ["author": personLinkage(dan())] | ["name"] | ["name": "Dan"] | Set.of()
    "an unrestricted primary fieldset" | [:] | ["title", "body-text"] | ["title": "Title", "body-text": "Body"] | ["comments", "author"] | ["comments": commentsLinkage(), "author": personLinkage(dan())] | ["name"] | ["name": "Dan"] | Set.of()
  }

  def "toMappedDocument keeps included absent when no include path is requested"() {
    given:
    def selection = selectionFor(["articles": ["title"]])

    when:
    def mapped = mapper.toMappedDocument(article(), null, selection, RepresentationPolicy.defaults())

    then:
    mapped.document().included() == null
    !mapped.document().hasIncludedMember()
  }

  def "toMappedDocument emits present-empty included when an include resolves to no resources"() {
    given:
    def selection = selectionFor([:], ["author"])
    def policy = RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(
        articleWithNullAuthor(), null, selection, policy)

    then:
    mapped.document().included() != null
    mapped.document().included().isEmpty()
    mapped.document().hasIncludedMember()
  }

  def "fieldset provenance identifies included resources whose linkage was omitted"() {
    given:
    def selection = selectionFor(["articles": ["title"]], ["author"])
    def policy = RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(article(), null, selection, policy)

    then:
    assertFieldsetResource(
        primaryResource(mapped), "articles", "1", ["title"], ["title": "Title"], null, [:])
    assertIncludedResource(mapped, "people", "9", ["name"], ["name": "Dan"], null, [:])
    mapped.sparseFieldsetLinkageExemptions() == Set.of(ResourceIdentity.ofId("people", "9"))
  }

  def "fieldset provenance is empty when the linking relationship survives"() {
    given:
    def selection = selectionFor(["articles": ["title", "author"]], ["author"])
    def policy = RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(article(), null, selection, policy)

    then:
    mapped.sparseFieldsetLinkageExemptions().isEmpty()
    primaryResource(mapped).relationships().relationships().keySet() == ["author"] as Set
  }

  @Unroll
  def "fieldset #description does not read excluded properties"() {
    given:
    def counting = new AccessCountingFieldsetArticle(
        "1", "Title", "Body", dan(), List.of(comment5()))
    def selection = selectionFor(fieldsets, includePaths)
    def policy = includePaths.isEmpty()
        ? RepresentationPolicy.defaults()
        : RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(counting, null, selection, policy)

    then:
    mapped != null
    counting.titleReads == titleReads
    counting.bodyReads == bodyReads
    counting.authorReads == authorReads
    counting.commentsReads == commentsReads

    where:
    description | fieldsets | includePaths | titleReads | bodyReads | authorReads | commentsReads
    "title-only without inclusion" | ["articles": ["title"]] | [] | 1 | 0 | 0 | 0
    "title-only with author inclusion" | ["articles": ["title"]] | ["author"] | 1 | 0 | 1 | 0
    "author-only without inclusion" | ["articles": ["author"]] | [] | 0 | 0 | 1 | 0
    "empty without inclusion" | ["articles": []] | [] | 0 | 0 | 0 | 0
  }

  def "selection snapshots the caller fieldset map and lists"() {
    given:
    def mutableFields = new ArrayList<>(["title", "title", "author"])
    def mutableFieldsets = new LinkedHashMap<String, List<String>>()
    mutableFieldsets.put("articles", mutableFields)
    def selection = selectionFor(mutableFieldsets)

    when:
    mutableFields.add("body-text")
    mutableFieldsets.put("people", ["name"])

    then:
    selection.fieldsets() == ["articles": ["title", "author"]]

    when:
    selection.fieldsets().get("articles").add("body-text")

    then:
    thrown(UnsupportedOperationException)
  }

  def "duplicate fieldset names collapse to first-seen order and map once"() {
    given:
    def fieldsets = ["articles": [
        "title",
        "title",
        "author",
        "title"
      ]]
    def selection = selectionFor(fieldsets)

    expect:
    selection.fieldsets() == ["articles": ["title", "author"]]

    when:
    def mapped = mapper.toMappedDocument(article(), null, selection, RepresentationPolicy.defaults())

    then:
    assertFieldsetResource(
        primaryResource(mapped),
        "articles",
        "1",
        ["title"],
        ["title": "Title"],
        ["author"],
        ["author": personLinkage(dan())])
  }

  def "field policy alone does not select fields"() {
    given:
    def policy = RepresentationPolicy.defaults().withFieldPolicy(FieldPolicy.denyAll())

    when:
    def mapped = mapper.toMappedDocument(
        article(), null, RepresentationSelection.none(), policy)

    then:
    assertFieldsetResource(
        primaryResource(mapped),
        "articles",
        "1",
        ["title", "body-text"],
        ["title": "Title", "body-text": "Body"],
        ["comments", "author"],
        ["comments": commentsLinkage(), "author": personLinkage(dan())])
    mapped.sparseFieldsetLinkageExemptions().isEmpty()
  }

  def "unmapped document rejects non-empty fieldsets"() {
    given:
    def selection = selectionFor(["articles": ["title"]])

    when:
    mapper.toDocument(article(), null, selection, RepresentationPolicy.defaults())

    then:
    def exception = thrown(JsonApiMappingException)
    exception.diagnostic() == MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT
    exception.propertyPath() == null
    exception.resourceClass() == null
    exception.message.contains("types: [articles]")
  }

  def "unmapped resource collection rejects non-empty fieldsets"() {
    given:
    def selection = selectionFor(["articles": ["title"]])

    when:
    mapper.toResourceCollection(
        [article()], null, selection, RepresentationPolicy.defaults())

    then:
    def exception = thrown(JsonApiMappingException)
    exception.diagnostic() == MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT
    exception.propertyPath() == null
    exception.resourceClass() == null
  }

  def "FieldAllowance permits selected wire fields and rejects other fields"() {
    given:
    def fieldPolicy = FieldPolicy.allowing(Set.of(FieldAllowance.of("articles", "title")))
    def allowedPolicy = RepresentationPolicy.defaults().withFieldPolicy(fieldPolicy)

    when:
    def mapped = mapper.toMappedDocument(
        article(), null, selectionFor(["articles": ["title"]]), allowedPolicy)

    then:
    primaryResource(mapped).attributes().attributes() == ["title": "Title"]

    when:
    mapper.toMappedDocument(
        article(),
        null,
        selectionFor(["articles": ["author"]]),
        allowedPolicy)

    then:
    def exception = thrown(JsonApiMappingException)
    exception.diagnostic() == MappingDiagnostic.DENIED_FIELDSET_FIELD
    exception.resourceClass() == Article.class
  }

  def "unknown field names win over field-policy denial"() {
    given:
    def selection = selectionFor(["articles": ["nope", "title"]])
    def policy = RepresentationPolicy.defaults().withFieldPolicy(FieldPolicy.denyAll())

    when:
    mapper.toMappedDocument(article(), null, selection, policy)

    then:
    def exception = thrown(JsonApiMappingException)
    exception.diagnostic() == MappingDiagnostic.INVALID_FIELDSET_FIELD
    exception.resourceClass() == Article.class
  }

  def "renamed fieldsets use configured Jackson wire names"() {
    when:
    def blog = mapper.toMappedDocument(
        new BlogWithJsonProperty("b1", "Hello"),
        null,
        selectionFor(["blogs": ["blog_title"]]),
        RepresentationPolicy.defaults())
    def article = mapper.toMappedDocument(
        new ArticleWithRenamedAuthor("1", "Title", dan()),
        null,
        selectionFor(["articles": ["written-by"]]),
        RepresentationPolicy.defaults())

    then:
    assertFieldsetResource(
        primaryResource(blog), "blogs", "b1", ["blog_title"], ["blog_title": "Hello"], null, [:])
    assertFieldsetResource(
        primaryResource(article),
        "articles",
        "1",
        null,
        [:],
        ["written-by"],
        ["written-by": personLinkage(dan())])
  }

  def "a renamed relationship rejects its Java logical name in a fieldset"() {
    when:
    mapper.toMappedDocument(
        new ArticleWithRenamedAuthor("1", "Title", dan()),
        null,
        selectionFor(["articles": ["author"]]),
        RepresentationPolicy.defaults())

    then:
    def exception = thrown(JsonApiMappingException)
    exception.diagnostic() == MappingDiagnostic.INVALID_FIELDSET_FIELD
    exception.resourceClass() == ArticleWithRenamedAuthor.class
  }

  def "nested included resources apply their own fieldset by type"() {
    given:
    def selection = selectionFor(
        ["comments": ["body"]],
        ["comments.author"])
    def policy = RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())

    when:
    def mapped = mapper.toMappedDocument(article(), null, selection, policy)

    then:
    assertFieldsetResource(
        primaryResource(mapped),
        "articles",
        "1",
        ["title", "body-text"],
        ["title": "Title", "body-text": "Body"],
        ["comments", "author"],
        ["comments": commentsLinkage(), "author": personLinkage(dan())])
    def included = mapped.document().included()
    included.size() == 4
    assertFieldsetResource(included[0], "comments", "5", ["body"], ["body": "First!"], null, [:])
    assertFieldsetResource(included[1], "comments", "12", ["body"], ["body": "I like XML better"], null, [:])
    assertFieldsetResource(included[2], "people", "2", ["name"], ["name": "Ezra"], null, [:])
    assertFieldsetResource(included[3], "people", "9", ["name"], ["name": "Dan"], null, [:])
    mapped.sparseFieldsetLinkageExemptions() == Set.of(
        ResourceIdentity.ofId("people", "2"), ResourceIdentity.ofId("people", "9"))
  }

  @Unroll
  def "fieldset #description preserves primary identity"() {
    when:
    def mapped = mapper.toMappedDocument(
        article(), null, selectionFor(fieldsets), RepresentationPolicy.defaults())

    then:
    primaryResource(mapped).type() == "articles"
    primaryResource(mapped).id() == "1"

    where:
    description | fieldsets
    "no fieldset" | [:]
    "an empty fieldset" | ["articles": []]
    "an attribute-only fieldset" | ["articles": ["title"]]
    "a relationship-only fieldset" | ["articles": ["author"]]
  }

  def "empty fieldsets retain resource meta independently of field policy"() {
    given:
    def article = new ArticleWithMeta(
        "1",
        "T",
        ResourceIdentifier.of("people", "p1"),
        new ArticleMeta("cms", "n"),
        null)
    def policy = RepresentationPolicy.defaults().withFieldPolicy(FieldPolicy.denyAll())

    when:
    def mapped = mapper.toMappedDocument(
        article, null, selectionFor(["articles": []]), policy)

    then:
    primaryResource(mapped).attributes() == null
    primaryResource(mapped).relationships() == null
    primaryResource(mapped).meta() == Meta.of(["source": "cms", "note": "n"])
  }

  def "concurrent fieldset mappings isolate documents and linkage exemptions"() {
    given:
    def shared = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
    def start = new CountDownLatch(1)
    def done = new CountDownLatch(2)
    def firstResult = new AtomicReference<MappedDocument>()
    def secondResult = new AtomicReference<MappedDocument>()
    def failure = new AtomicReference<Throwable>()
    def pool = Executors.newFixedThreadPool(2)

    when:
    pool.submit({
      try {
        start.await()
        100.times {
          firstResult.set(shared.toMappedDocument(
              article(),
              null,
              selectionFor(["articles": ["title"]], ["author"]),
              RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())))
        }
      } catch (Throwable throwable) {
        failure.compareAndSet(null, throwable)
      } finally {
        done.countDown()
      }
    } as Runnable)
    pool.submit({
      try {
        start.await()
        100.times {
          secondResult.set(shared.toMappedDocument(
              article(),
              null,
              selectionFor(["articles": ["title", "author", "comments"]], ["author"]),
              RepresentationPolicy.defaults().withIncludePolicy(IncludePolicy.allowAll())))
        }
      } catch (Throwable throwable) {
        failure.compareAndSet(null, throwable)
      } finally {
        done.countDown()
      }
    } as Runnable)
    start.countDown()

    then:
    done.await(10, TimeUnit.SECONDS)
    failure.get() == null
    firstResult.get().sparseFieldsetLinkageExemptions() == Set.of(ResourceIdentity.ofId("people", "9"))
    secondResult.get().sparseFieldsetLinkageExemptions().isEmpty()
    primaryResource(firstResult.get()).attributes().attributes() == ["title": "Title"]
    primaryResource(secondResult.get()).attributes().attributes() == ["title": "Title"]

    cleanup:
    pool.shutdownNow()
  }

  private static RepresentationSelection selectionFor(
      Map<String, List<String>> fieldsets, List<String> includePaths = []) {
    def builder = RepresentationSelection.builder()
    includePaths.each { path -> builder.include(IncludePath.of(path as String)) }
    fieldsets.each { type, fields ->
      builder.fields(type as String, fields as List<String>)
    }
    builder.build()
  }

  private static ResourceObject primaryResource(MappedDocument mapped) {
    def data = mapped.document().data()
    assert data instanceof DocumentData.SingleResource
    ((DocumentData.SingleResource) data).resource()
  }

  private static List<ResourceObject> primaryResources(MappedDocument mapped) {
    def data = mapped.document().data()
    assert data instanceof DocumentData.ResourceCollection
    ((DocumentData.ResourceCollection) data).resources()
  }

  private static void assertIncludedResource(
      MappedDocument mapped,
      String expectedType,
      String expectedId,
      List<String> expectedAttributeNames,
      Map<String, Object> expectedAttributes,
      List<String> expectedRelationshipNames,
      Map<String, RelationshipData> expectedLinkage) {
    def included = mapped.document().included()
    assert included != null
    assert included.size() == 1
    assertFieldsetResource(
        included[0],
        expectedType,
        expectedId,
        expectedAttributeNames,
        expectedAttributes,
        expectedRelationshipNames,
        expectedLinkage)
  }

  private static void assertFieldsetResource(
      ResourceObject actual,
      String expectedType,
      String expectedId,
      List<String> expectedAttributeNames,
      Map<String, Object> expectedAttributes,
      List<String> expectedRelationshipNames,
      Map<String, RelationshipData> expectedLinkage) {
    assert actual.type() == expectedType
    assert actual.id() == expectedId

    if (expectedAttributeNames == null) {
      assert actual.attributes() == null
    } else {
      assert actual.attributes() != null
      assert List.copyOf(actual.attributes().attributes().keySet()) == expectedAttributeNames
      assert actual.attributes().attributes() == expectedAttributes
    }

    if (expectedRelationshipNames == null) {
      assert actual.relationships() == null
    } else {
      assert actual.relationships() != null
      assert List.copyOf(actual.relationships().relationships().keySet()) == expectedRelationshipNames
      expectedLinkage.each { name, expectedData ->
        assert actual.relationships().relationships().get(name) != null
        assert actual.relationships().relationships().get(name).data() == expectedData
      }
    }
  }

  private static Article article() {
    new Article("1", "Title", "Body", List.of(comment5(), comment12()), dan())
  }

  private static Article articleWithNullAuthor() {
    new Article("1", "Title", "Body", List.of(comment5()), null)
  }

  private static Person dan() {
    new Person("9", "Dan")
  }

  private static Comment comment5() {
    new Comment("5", "First!", new Person("2", "Ezra"))
  }

  private static Comment comment12() {
    new Comment("12", "I like XML better", dan())
  }

  private static RelationshipData personLinkage(Person person) {
    new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", person.id()))
  }

  private static RelationshipData commentsLinkage() {
    new RelationshipData.IdentifierCollectionLinkage(
        List.of(ResourceIdentifier.of("comments", "5"), ResourceIdentifier.of("comments", "12")))
  }
}
