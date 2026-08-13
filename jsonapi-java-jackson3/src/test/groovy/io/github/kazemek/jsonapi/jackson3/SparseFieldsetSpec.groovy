package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.CompoundSerializationContext
import io.github.kazemek.jsonapi.jackson.FieldAllowance
import io.github.kazemek.jsonapi.jackson.FieldPolicy
import io.github.kazemek.jsonapi.jackson.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.MappedDocument
import io.github.kazemek.jsonapi.jackson.MappingDiagnostic
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.AccessCountingFieldsetArticle
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.FieldsetResourceState
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetExpectation
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetOperation
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetRequest
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetScenario
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetScenarios
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetSide
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.lang.Unroll
import tools.jackson.databind.json.JsonMapper

// Shared sparse-fieldset cases live in SparseFieldsetScenarios. This spec runs every catalog
// entry and asserts executedScenarioIds == catalogScenarioIds so a later Jackson 2 suite can do
// the same. Adapter-local scenario content remains empty unless a major-mapper-only case appears;
// suite-local harness assertions (fieldset-map and FieldAllowance mutation isolation, duplicate
// collapse, FIELDSETS_REQUIRE_MAPPED_DOCUMENT message composition, exact single-read counts, and
// applyTo/writer-validation) stay here.
// @Stepwise pins the declared feature order so the coverage feature always runs after the
// parameterized catalog iterations (Spock does not guarantee feature order otherwise).
@Stepwise
class SparseFieldsetSpec extends Specification {

  @Shared
  JsonApiResourceMapper mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

  @Shared
  def jackson = JsonMapper.builder().build()

  @Shared
  Set<String> executedScenarioIds = new LinkedHashSet<>()

  @Unroll
  def "sparse fieldset #scenario.id from the shared catalog"() {
    given:
    executedScenarioIds.add(scenario.id())

    when:
    def thrownException = null
    def mapped = null
    def document = null
    try {
      def result = execute(scenario)
      if (result instanceof MappedDocument) {
        mapped = result
      } else {
        document = result
      }
    } catch (Exception e) {
      thrownException = e
    }

    then:
    verify(scenario, mapped, document, thrownException)

    where:
    scenario << SparseFieldsetScenarios.all()
  }

  def "covers every shared sparse-fieldset scenario exactly once"() {
    expect:
    executedScenarioIds == SparseFieldsetScenarios.all()*.id as Set
  }

  def "defensive copy isolates the caller's fieldset map after context construction"() {
    given:
    def mutableFields = new ArrayList<>(["title", "title", "author"])
    def mutableMap = new LinkedHashMap<String, List<String>>()
    mutableMap.put("articles", mutableFields)
    def context = CompoundSerializationContext.defaults().withFieldsets(mutableMap)

    when:
    mutableFields.add("body-text")
    mutableMap.put("people", ["name"])

    then:
    context.fieldsets().get("articles") == ["title", "author"]
    !context.fieldsets().containsKey("people")
  }

  def "duplicate fieldset names collapse to first-seen order"() {
    given:
    def scenario = SparseFieldsetScenarios.byId(
        "defensive copy isolates fieldset map and duplicate names collapse")
    def context = ((SparseFieldsetRequest.Single) scenario.request()).context()

    expect:
    context.fieldsets().get("articles") == ["title", "author"]
  }

  def "FieldAllowance caller set mutation does not enlarge the policy"() {
    given:
    def allowances = new HashSet<>([
      FieldAllowance.of("articles", "title")
    ])
    def policy = FieldPolicy.allowing(allowances)
    def context = CompoundSerializationContext.defaults()
        .withFieldsets([articles: ["title"]])
        .withFieldPolicy(policy)

    when:
    allowances.add(FieldAllowance.of("articles", "author"))
    def mapped = mapper.toMappedDocument(
        ((SparseFieldsetRequest.Single) SparseFieldsetScenarios.byId(
        "FieldAllowance-satisfied fieldset succeeds").request()).supplier().get(),
        null,
        context)

    then:
    mapped.document() != null

    when:
    mapper.toMappedDocument(
        ((SparseFieldsetRequest.Single) SparseFieldsetScenarios.byId(
        "FieldAllowance denies a present field not in the allowance set").request()).supplier().get(),
        null,
        CompoundSerializationContext.defaults()
        .withFieldsets([articles: ["author"]])
        .withFieldPolicy(policy))

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.DENIED_FIELDSET_FIELD
  }

  def "toDocument FIELDSETS_REQUIRE_MAPPED_DOCUMENT message names the fieldset types"() {
    given:
    def scenario = SparseFieldsetScenarios.byId(
        "three-argument toDocument rejects non-empty fieldsets")
    def request = (SparseFieldsetRequest.Single) scenario.request()

    when:
    mapper.toDocument(request.supplier().get(), null, request.context())

    then:
    def e = thrown(JsonApiMappingException)
    e.diagnostic() == MappingDiagnostic.FIELDSETS_REQUIRE_MAPPED_DOCUMENT
    e.message.contains("types: [articles]")
  }

  def "access counting exact single-read counts remain Jackson 3 suite-local"() {
    given:
    def scenario = SparseFieldsetScenarios.byId(
        "access counting proves linkage vs traversal split")
    def request = (SparseFieldsetRequest.Single) scenario.request()
    def counting = (AccessCountingFieldsetArticle) request.supplier().get()

    when:
    mapper.toMappedDocument(counting, null, request.context())

    then:
    counting.titleReads == 1
    counting.authorReads == 1
    counting.bodyReads == 0
    counting.commentsReads == 0
  }

  def "applyTo enables full-linkage exception for omitted relationships"() {
    given:
    def scenario = SparseFieldsetScenarios.byId(
        "include author with fields articles title omits linkage and sets exception")
    def request = (SparseFieldsetRequest.Single) scenario.request()
    def mapped = mapper.toMappedDocument(request.supplier().get(), null, request.context())

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

  def "applyTo returns the same ValidationContext instance when the exception flag is false"() {
    given:
    def scenario = SparseFieldsetScenarios.byId(
        "applyTo leaves base unchanged when exception flag is false")
    def request = (SparseFieldsetRequest.Single) scenario.request()
    def mapped = mapper.toMappedDocument(request.supplier().get(), null, request.context())
    def base = ValidationContext.defaults()

    expect:
    !mapped.sparseFieldsetException()
    mapped.applyTo(base).is(base)
  }

  private Object execute(SparseFieldsetScenario scenario) {
    def request = scenario.request()
    if (request instanceof SparseFieldsetRequest.Concurrent) {
      executeConcurrent(
          request, (SparseFieldsetExpectation.ConcurrentIsolation) scenario.expectation())
      return null
    }
    if (request instanceof SparseFieldsetRequest.IdentityPreservation) {
      executeIdentity(request, (SparseFieldsetExpectation.IdentityPreservation) scenario.expectation())
      return null
    }
    return invoke(scenario.operation(), request)
  }

  private Object invoke(SparseFieldsetOperation operation, SparseFieldsetRequest request) {
    switch (operation) {
      case SparseFieldsetOperation.TO_DOCUMENT:
        def documentRequest = (SparseFieldsetRequest.Single) request
        return mapper.toDocument(documentRequest.supplier().get(), null, documentRequest.context())
      case SparseFieldsetOperation.TO_RESOURCE_COLLECTION:
        def collectionRequest = (SparseFieldsetRequest.Collection) request
        return mapper.toResourceCollection(
            collectionRequest.supplier().get(), null, collectionRequest.context())
      case SparseFieldsetOperation.TO_MAPPED_DOCUMENT:
        def mappedDocumentRequest = (SparseFieldsetRequest.Single) request
        return mapper.toMappedDocument(
            mappedDocumentRequest.supplier().get(), null, mappedDocumentRequest.context())
      case SparseFieldsetOperation.TO_MAPPED_RESOURCE_COLLECTION:
        def mappedCollectionRequest = (SparseFieldsetRequest.Collection) request
        return mapper.toMappedResourceCollection(
            mappedCollectionRequest.supplier().get(), null, mappedCollectionRequest.context())
      default:
        throw new IllegalArgumentException("Unknown operation: " + operation)
    }
  }

  private void executeIdentity(
      SparseFieldsetRequest.IdentityPreservation request,
      SparseFieldsetExpectation.IdentityPreservation expectation) {
    request.contexts().each { context ->
      def mapped = mapper.toMappedDocument(request.supplier().get(), null, context)
      def resource = (mapped.document().data() as DocumentData.SingleResource).resource()
      assert resource.type() == expectation.type()
      assert resource.id() == expectation.id()
    }
  }

  private void executeConcurrent(
      SparseFieldsetRequest.Concurrent request,
      SparseFieldsetExpectation.ConcurrentIsolation isolation) {
    def pool = Executors.newFixedThreadPool(2)
    def start = new CountDownLatch(1)
    def done = new CountDownLatch(2)
    def omitResult = new AtomicReference<MappedDocument>()
    def linkResult = new AtomicReference<MappedDocument>()
    def error = new AtomicReference<Throwable>()
    try {
      pool.submit(concurrentTask(request.first(), omitResult, start, done, error) as Runnable)
      pool.submit(concurrentTask(request.second(), linkResult, start, done, error) as Runnable)
      start.countDown()
      assert done.await(10, TimeUnit.SECONDS)
      assert error.get() == null
      verifyMapped(isolation.first(), omitResult.get())
      verifyMapped(isolation.second(), linkResult.get())
    } finally {
      pool.shutdownNow()
    }
  }

  private Closure concurrentTask(
      SparseFieldsetSide side,
      AtomicReference<MappedDocument> result,
      CountDownLatch start,
      CountDownLatch done,
      AtomicReference<Throwable> error) {
    return {
      try {
        start.await()
        result.set(mapper.toMappedDocument(side.supplier().get(), null, side.context()))
      } catch (Throwable t) {
        error.compareAndSet(null, t)
      } finally {
        done.countDown()
      }
    }
  }

  private void verify(
      SparseFieldsetScenario scenario,
      MappedDocument mapped,
      def document,
      Throwable thrownException) {
    def expectation = scenario.expectation()
    if (expectation instanceof SparseFieldsetExpectation.Failure) {
      assert thrownException instanceof JsonApiMappingException
      def ex = (JsonApiMappingException) thrownException
      assert ex.diagnostic() == expectation.diagnostic()
      assert ex.propertyPath() == expectation.propertyPath()
      assert ex.resourceClass() == expectation.resourceClass()
      return
    }
    if (expectation instanceof SparseFieldsetExpectation.ConcurrentIsolation
        || expectation instanceof SparseFieldsetExpectation.IdentityPreservation) {
      assert thrownException == null
      return
    }
    assert thrownException == null
    if (expectation instanceof SparseFieldsetExpectation.UnmappedSuccess) {
      assert document != null
      def resource = primaryResource(document.data())
      assertResource(expectation.primary(), resource)
      assertIncluded(document.included(), expectation.included())
      return
    }
    def success = (SparseFieldsetExpectation.MappedSuccess) expectation
    verifyMapped(success, mapped)
    def request = scenario.request()
    if (success.zeroReads() != null && request instanceof SparseFieldsetRequest.Single) {
      // Catalog iteration already consumed a fresh instance; re-run to observe counters.
      def counting = (AccessCountingFieldsetArticle) request.supplier().get()
      mapper.toMappedDocument(counting, null, request.context())
      success.zeroReads().unreadAttributes().each { name ->
        assert readsFor(counting, name) == 0
      }
      success.zeroReads().unreadRelationships().each { name ->
        assert readsFor(counting, name) == 0
      }
    }
  }

  private static void verifyMapped(
      SparseFieldsetExpectation.MappedSuccess success, MappedDocument mapped) {
    assert mapped != null
    assert mapped.sparseFieldsetException() == success.sparseFieldsetException()
    def resource = primaryResource(mapped.document().data())
    assertResource(success.primary(), resource)
    assertIncluded(mapped.document().included(), success.included())
  }

  private static ResourceObject primaryResource(def data) {
    if (data instanceof DocumentData.SingleResource) {
      return data.resource()
    }
    if (data instanceof DocumentData.ResourceCollection) {
      return data.resources()[0]
    }
    throw new IllegalArgumentException("Unsupported primary data: " + data)
  }

  private static void assertResource(FieldsetResourceState expected, ResourceObject actual) {
    assert actual.type() == expected.type()
    assert actual.id() == expected.id()
    if (expected.attributeNames() == null) {
      assert actual.attributes() == null
    } else {
      assert actual.attributes() != null
      assert actual.attributes().attributes().keySet() as List == expected.attributeNames()
      expected.attributeValues().each { name, value ->
        assert actual.attributes().attributes().get(name) == value
      }
    }
    if (expected.relationshipNames() == null) {
      assert actual.relationships() == null
    } else {
      assert actual.relationships() != null
      assert actual.relationships().relationships().keySet() as List == expected.relationshipNames()
      expected.relationshipLinkage().each { name, data ->
        Relationship relationship = actual.relationships().relationships().get(name)
        assert relationship.data() == data
      }
    }
  }

  private static void assertIncluded(
      List<ResourceObject> actual, List<FieldsetResourceState> expected) {
    if (expected == null) {
      assert actual == null
      return
    }
    assert actual != null
    assert actual.size() == expected.size()
    expected.eachWithIndex { FieldsetResourceState state, int i ->
      assertResource(state, actual[i])
    }
  }

  private static int readsFor(AccessCountingFieldsetArticle counting, String field) {
    switch (field) {
      case "title":
        return counting.titleReads
      case "body":
        return counting.bodyReads
      case "author":
        return counting.authorReads
      case "comments":
        return counting.commentsReads
      default:
        throw new IllegalArgumentException("Unknown access-count field: " + field)
    }
  }
}
