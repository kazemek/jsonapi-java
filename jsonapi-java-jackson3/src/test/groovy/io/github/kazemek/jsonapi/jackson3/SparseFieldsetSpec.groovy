package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext
import io.github.kazemek.jsonapi.jackson.FieldAllowance
import io.github.kazemek.jsonapi.jackson.FieldPolicy
import io.github.kazemek.jsonapi.jackson.IncludePath
import io.github.kazemek.jsonapi.jackson.IncludePolicy
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappedDocument
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.jackson3.testmodel.AccessCountingFieldsetArticle
import io.github.kazemek.jsonapi.jackson3.testmodel.Article
import io.github.kazemek.jsonapi.jackson3.testmodel.ArticleWithRenamedAuthor
import io.github.kazemek.jsonapi.jackson3.testmodel.BlogWithJsonProperty
import io.github.kazemek.jsonapi.jackson3.testmodel.Comment
import io.github.kazemek.jsonapi.jackson3.testmodel.Person
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Specification
import tools.jackson.databind.json.JsonMapper

class SparseFieldsetSpec extends Specification {

  def mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())
  def jackson = JsonMapper.builder().build()

  def dan = new Person("9", "Dan")
  def ezra = new Person("2", "Ezra")
  def comment5 = new Comment("5", "First!", ezra)
  def comment12 = new Comment("12", "I like XML better", dan)
  def article = new Article("1", "Title", "Body", [comment5, comment12], dan)

  private static CompoundSerializationContext fieldsets(Map<String, List<String>> fields) {
    return CompoundSerializationContext.defaults().withFieldsets(fields)
  }

  private static CompoundSerializationContext includeAndFields(
      List<String> paths, Map<String, List<String>> fields) {
    return CompoundSerializationContext.defaults()
        .withIncludePaths(paths.collect { IncludePath.of(it) })
        .withIncludePolicy(IncludePolicy.allowAll())
        .withFieldsets(fields)
  }

  def "unrestricted MappedDocument matches Phase 2.2 attributes and relationships"() {
    when:
    def full = mapper.toDocument(article)
    def mapped = mapper.toMappedDocument(article, null, CompoundSerializationContext.defaults())

    then:
    !mapped.sparseFieldsetException()
    mapped.document().data() == full.data()
    mapped.document().included() == null
  }

  def "three-argument toDocument with empty fieldset map remains Phase 2.3 equivalent"() {
    given:
    def context = CompoundSerializationContext.defaults()
        .withIncludePaths([IncludePath.of("author")])
        .withIncludePolicy(IncludePolicy.allowAll())

    when:
    def doc = mapper.toDocument(article, null, context)

    then:
    doc.included()*.id() == ["9"]
    (doc.data() as DocumentData.SingleResource).resource().relationships()
        .relationships().containsKey("author")
  }

  def "present empty list emits identity-only primary"() {
    when:
    def mapped = mapper.toMappedDocument(article, null, fieldsets([articles: []]))

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.type() == "articles"
    resource.id() == "1"
    resource.attributes() == null
    resource.relationships() == null
    mapped.sparseFieldsetException()
  }

  def "present empty list emits identity-only included when that type appears"() {
    given:
    def context = includeAndFields(["author"], [people: []])

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    def primary = (mapped.document().data() as DocumentData.SingleResource).resource()
    primary.attributes().attributes().containsKey("title")
    primary.relationships().relationships().containsKey("author")
    mapped.document().included().size() == 1
    def included = mapped.document().included()[0]
    included.type() == "people"
    included.id() == "9"
    included.attributes() == null
    included.relationships() == null
    !mapped.sparseFieldsetException()
  }

  def "present empty list with denyAll succeeds without DENIED_FIELDSET_FIELD"() {
    given:
    def context = fieldsets([articles: []]).withFieldPolicy(FieldPolicy.denyAll())

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes() == null
    resource.relationships() == null
    mapped.sparseFieldsetException()
  }

  def "three-argument toDocument rejects non-empty fieldsets"() {
    when:
    mapper.toDocument(article, null, fieldsets([articles: ["title"]]))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT
    e.message.contains("types: [articles]")
  }

  def "three-argument toResourceCollection rejects non-empty fieldsets"() {
    when:
    mapper.toResourceCollection([article], null, fieldsets([articles: ["title"]]))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT
  }

  def "attribute-only fieldset via toMappedDocument"() {
    when:
    def mapped = mapper.toMappedDocument(article, null, fieldsets([articles: ["title"]]))

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes().attributes().keySet() as List == ["title"]
    resource.relationships() == null
    mapped.sparseFieldsetException()
  }

  def "relationship-only fieldset via toMappedResourceCollection"() {
    when:
    def mapped = mapper.toMappedResourceCollection(
        [article], null, fieldsets([articles: ["author"]]))

    then:
    def resource = (mapped.document().data() as DocumentData.ResourceCollection).resources()[0]
    resource.attributes() == null
    resource.relationships().relationships().keySet() as List == ["author"]
    mapped.sparseFieldsetException()
  }

  def "renamed JsonProperty fieldset names use final JSON:API names"() {
    given:
    def blog = new BlogWithJsonProperty("b1", "Hello")

    when:
    def mapped = mapper.toMappedDocument(blog, null, fieldsets([blogs: ["blog_title"]]))

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes().attributes().get("blog_title") == "Hello"
  }

  def "renamed JsonApiAttribute fieldset uses body-text"() {
    when:
    def mapped = mapper.toMappedDocument(article, null, fieldsets([articles: ["body-text"]]))

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes().attributes().get("body-text") == "Body"
    !resource.attributes().attributes().containsKey("title")
  }

  def "renamed JsonApiRelationship fieldset uses written-by"() {
    given:
    def renamed = new ArticleWithRenamedAuthor("1", "Title", dan)

    when:
    def mapped = mapper.toMappedDocument(
        renamed, null, fieldsets([articles: ["written-by"]]))

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes() == null
    resource.relationships().relationships().keySet() as List == ["written-by"]
    def linkage = (RelationshipData.SingleLinkage) resource.relationships()
        .relationships().get("written-by").data()
    linkage.identifier().id() == "9"
  }

  def "unknown JsonApiRelationship rename fails against Java property name"() {
    given:
    def renamed = new ArticleWithRenamedAuthor("1", "Title", dan)

    when:
    mapper.toMappedDocument(renamed, null, fieldsets([articles: ["author"]]))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_FIELDSET_FIELD
    e.propertyPath() == "author"
  }

  def "per-type fieldsets do not strip unrelated included types"() {
    given:
    def context = includeAndFields(["author"], [articles: ["title"]])

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    def primary = (mapped.document().data() as DocumentData.SingleResource).resource()
    primary.attributes().attributes().keySet() as List == ["title"]
    primary.relationships() == null
    mapped.document().included().size() == 1
    mapped.document().included()[0].type() == "people"
    mapped.document().included()[0].attributes().attributes().containsKey("name")
    mapped.sparseFieldsetException()
  }

  def "include author with fields articles title omits linkage and sets exception"() {
    given:
    def context = includeAndFields(["author"], [articles: ["title"]])

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    def primary = (mapped.document().data() as DocumentData.SingleResource).resource()
    primary.relationships() == null
    mapped.document().included()*.id() == ["9"]
    mapped.sparseFieldsetException()

    when:
    JsonApiJackson3.writer(jackson, mapped.applyTo(ValidationContext.defaults()))
        .writeValueAsString(mapped.document())

    then:
    noExceptionThrown()

    when:
    JsonApiJackson3.writer(jackson, ValidationContext.defaults())
        .writeValueAsString(mapped.document())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.FULL_LINKAGE_VIOLATION
  }

  def "nested include comments.author with fields comments body"() {
    given:
    def context = includeAndFields(["comments.author"], [comments: ["body"]])

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    mapped.document().included()*.type() == [
      "comments",
      "comments",
      "people",
      "people"
    ]
    mapped.document().included().findAll { it.type() == "comments" }.every {
      (it.attributes().attributes().keySet() as List) == ["body"] && it.relationships() == null
    }
    mapped.document().included().findAll { it.type() == "people" }.every {
      it.attributes().attributes().containsKey("name")
    }
    mapped.sparseFieldsetException()
  }

  def "attribute-only omission with fully linked includes keeps exception false"() {
    given:
    def context = includeAndFields(
        ["author"], [articles: ["title", "author", "comments"]])

    when:
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    def primary = (mapped.document().data() as DocumentData.SingleResource).resource()
    primary.attributes().attributes().keySet() as List == ["title"]
    primary.relationships().relationships().keySet() as Set == ["author", "comments"] as Set
    mapped.document().included()*.id() == ["9"]
    !mapped.sparseFieldsetException()
  }

  def "access counting proves linkage vs traversal split"() {
    given:
    def counting = new AccessCountingFieldsetArticle(
        "1", "Title", "Body", dan, [comment5])
    def context = includeAndFields(["author"], [articles: ["title"]])

    when:
    def mapped = mapper.toMappedDocument(counting, null, context)

    then:
    def primary = (mapped.document().data() as DocumentData.SingleResource).resource()
    primary.attributes().attributes().keySet() as List == ["title"]
    primary.relationships() == null
    mapped.document().included()*.id() == ["9"]
    counting.titleReads == 1
    counting.bodyReads == 0
    counting.authorReads == 1
    counting.commentsReads == 0
  }

  def "unknown fieldset field fails with INVALID_FIELDSET_FIELD"() {
    when:
    mapper.toMappedDocument(article, null, fieldsets([articles: ["nope"]]))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_FIELDSET_FIELD
    e.resourceClass() == Article
    e.propertyPath() == "nope"
  }

  def "denyAll rejects first present fieldset name"() {
    given:
    def context = fieldsets([articles: ["title", "author"]])
    .withFieldPolicy(FieldPolicy.denyAll())

    when:
    mapper.toMappedDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.DENIED_FIELDSET_FIELD
    e.resourceClass() == Article
    e.propertyPath() == "title"
  }

  def "missing FieldAllowance denies with DENIED_FIELDSET_FIELD"() {
    given:
    def context = fieldsets([articles: ["title", "author"]])
    .withFieldPolicy(FieldPolicy.allowing(Set.of(
    FieldAllowance.of("articles", "title"))))

    when:
    mapper.toMappedDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.DENIED_FIELDSET_FIELD
    e.propertyPath() == "author"
  }

  def "unmapped name wins over policy denial"() {
    given:
    def context = fieldsets([articles: ["nope", "title"]])
    .withFieldPolicy(FieldPolicy.denyAll())

    when:
    mapper.toMappedDocument(article, null, context)

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.INVALID_FIELDSET_FIELD
    e.propertyPath() == "nope"
  }

  def "unused fieldset type keys are ignored"() {
    when:
    def mapped = mapper.toMappedDocument(
        article, null, fieldsets([tags: ["name"], articles: ["title"]]))

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes().attributes().keySet() as List == ["title"]
  }

  def "defensive copy isolates fieldset map and duplicate names collapse"() {
    given:
    def mutableFields = new ArrayList<>(["title", "title", "author"])
    def mutableMap = new LinkedHashMap<String, List<String>>()
    mutableMap.put("articles", mutableFields)
    def context = fieldsets(mutableMap)

    when:
    mutableFields.add("body-text")
    mutableMap.put("people", ["name"])
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    context.fieldsets().get("articles") == ["title", "author"]
    !context.fieldsets().containsKey("people")
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes().attributes().keySet() as List == ["title"]
    resource.relationships().relationships().keySet() as List == ["author"]
  }

  def "FieldAllowance set is defensively copied"() {
    given:
    def allowances = new HashSet<>([
      FieldAllowance.of("articles", "title")
    ])
    def policy = FieldPolicy.allowing(allowances)
    def context = fieldsets([articles: ["title"]]).withFieldPolicy(policy)

    when:
    allowances.add(FieldAllowance.of("articles", "author"))
    def mapped = mapper.toMappedDocument(article, null, context)

    then:
    mapped.document() != null

    when:
    mapper.toMappedDocument(
        article, null, fieldsets([articles: ["author"]]).withFieldPolicy(policy))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.DENIED_FIELDSET_FIELD
  }

  def "identity preserved under every fieldset shape"() {
    expect:
    identityOnly(fieldsets([:]))
    identityOnly(fieldsets([articles: []]))
    identityOnly(fieldsets([articles: ["title"]]))
    identityOnly(fieldsets([articles: ["author"]]))
  }

  private void identityOnly(CompoundSerializationContext context) {
    def mapped = mapper.toMappedDocument(article, null, context)
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    assert resource.type() == "articles"
    assert resource.id() == "1"
  }

  def "surviving fields keep mapping definition order"() {
    when:
    def mapped = mapper.toMappedDocument(
        article, null, fieldsets([articles: [
            "author",
            "body-text",
            "title"
          ]]))

    then:
    def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
    resource.attributes().attributes().keySet() as List == ["title", "body-text"]
    resource.relationships().relationships().keySet() as List == ["author"]
  }

  def "concurrent fieldset mappings isolate documents and exception flags"() {
    given:
    def omitting = includeAndFields(["author"], [articles: ["title"]])
    def linked = includeAndFields(
        ["author"], [articles: ["title", "author", "comments"]])
    def pool = Executors.newFixedThreadPool(2)
    def start = new CountDownLatch(1)
    def done = new CountDownLatch(2)
    def omitResult = new AtomicReference<MappedDocument>()
    def linkResult = new AtomicReference<MappedDocument>()
    def error = new AtomicReference<Throwable>()

    pool.submit {
      try {
        start.await()
        omitResult.set(mapper.toMappedDocument(article, null, omitting))
      } catch (Throwable t) {
        error.compareAndSet(null, t)
      } finally {
        done.countDown()
      }
    }
    pool.submit {
      try {
        start.await()
        linkResult.set(mapper.toMappedDocument(article, null, linked))
      } catch (Throwable t) {
        error.compareAndSet(null, t)
      } finally {
        done.countDown()
      }
    }

    when:
    start.countDown()
    def completed = done.await(10, TimeUnit.SECONDS)
    pool.shutdown()

    then:
    completed
    error.get() == null
    omitResult.get().sparseFieldsetException()
    !linkResult.get().sparseFieldsetException()
    (omitResult.get().document().data() as DocumentData.SingleResource).resource()
        .relationships() == null
    (linkResult.get().document().data() as DocumentData.SingleResource).resource()
        .relationships().relationships().containsKey("author")
  }

  def "applyTo leaves base unchanged when exception flag is false"() {
    given:
    def mapped = mapper.toMappedDocument(
        article, null, fieldsets([articles: [
            "title",
            "author",
            "comments",
            "body-text"
          ]]))
    def base = ValidationContext.defaults()

    expect:
    !mapped.sparseFieldsetException()
    mapped.applyTo(base).is(base)
  }
}
