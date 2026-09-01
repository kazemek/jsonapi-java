package io.github.kazemek.jsonapi.jackson3

import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceIdentity
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.core.validation.DocumentUsage
import io.github.kazemek.jsonapi.core.validation.JsonApiValidationException
import io.github.kazemek.jsonapi.core.validation.LinksContext
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.core.validation.ValidationRuleCode
import io.github.kazemek.jsonapi.jackson.representation.CompoundSerializationContext
import io.github.kazemek.jsonapi.jackson.document.DocumentEnvelope
import io.github.kazemek.jsonapi.jackson.representation.FieldAllowance
import io.github.kazemek.jsonapi.jackson.representation.FieldPolicy
import io.github.kazemek.jsonapi.jackson.representation.IncludePath
import io.github.kazemek.jsonapi.jackson.representation.IncludePolicy
import io.github.kazemek.jsonapi.jackson.diagnostic.JsonApiMappingException
import io.github.kazemek.jsonapi.jackson.mapping.MappedDocument
import io.github.kazemek.jsonapi.jackson.diagnostic.MappingDiagnostic
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Article
import io.github.kazemek.jsonapi.testsupport.fixtures.domainwrite.Person
import io.github.kazemek.jsonapi.testsupport.fixtures.sparsefieldset.AccessCountingFieldsetArticle
import io.github.kazemek.jsonapi.testsupport.sparsefieldset.FieldsetResourceState
import io.github.kazemek.jsonapi.testsupport.sparsefieldset.SparseFieldsetExpectation
import io.github.kazemek.jsonapi.testsupport.sparsefieldset.SparseFieldsetOperation
import io.github.kazemek.jsonapi.testsupport.sparsefieldset.SparseFieldsetRequest
import io.github.kazemek.jsonapi.testsupport.sparsefieldset.SparseFieldsetScenario
import io.github.kazemek.jsonapi.testsupport.sparsefieldset.SparseFieldsetScenarios
import io.github.kazemek.jsonapi.testsupport.sparsefieldset.SparseFieldsetSide
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import tools.jackson.databind.json.JsonMapper

// Shared sparse-fieldset cases live in SparseFieldsetScenarios. This spec runs every catalog
// entry directly through this adapter's mapper; adding a scenario to the shared catalog is picked
// up automatically. Adapter-local scenario content remains empty unless a major-mapper-only case
// appears; suite-local harness assertions (fieldset-map and FieldAllowance mutation isolation,
// duplicate collapse, FIELDSETS_REQUIRE_MAPPED_DOCUMENT message composition, exact single-read
// counts, and writer-owned provenance composition/validation) stay here.
class SparseFieldsetSpec extends Specification {

  @Shared
  JsonApiResourceMapper mapper = JsonApiJackson3.resourceMapper(JsonMapper.builder().build())

  @Shared
  def jackson = JsonMapper.builder().build()

  @Unroll
  def "sparse fieldset #scenario.id from the shared catalog"() {
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
    scenario << SparseFieldsetScenarios.catalog().all()
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
    def scenario = SparseFieldsetScenarios.catalog().byId(
        "duplicate-free multi-field fieldset keeps title and author")
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
        ((SparseFieldsetRequest.Single) SparseFieldsetScenarios.catalog().byId(
        "FieldAllowance-satisfied fieldset succeeds").request()).supplier().get(),
        null,
        context)

    then:
    mapped.document() != null

    when:
    mapper.toMappedDocument(
        ((SparseFieldsetRequest.Single) SparseFieldsetScenarios.catalog().byId(
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
    def scenario = SparseFieldsetScenarios.catalog().byId(
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
    def scenario = SparseFieldsetScenarios.catalog().byId(
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

  def "writer composes sparse-fieldset provenance without caller choreography"() {
    given:
    def scenario = SparseFieldsetScenarios.catalog().byId(
        "include author with fields articles title omits linkage and sets exception")
    def request = (SparseFieldsetRequest.Single) scenario.request()
    def mapped = mapper.toMappedDocument(request.supplier().get(), null, request.context())

    when:
    JsonApiJackson3.writer(jackson).writeValueAsString(mapped)

    then:
    noExceptionThrown()

    when:
    JsonApiJackson3.writer(jackson).writeValueAsString(mapped.document())

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.FULL_LINKAGE_VIOLATION
  }

  def "mapped writing preserves unrelated caller validation settings"() {
    given:
    def article = new Article("1", "Title", "Body", List.of(), new Person("9", "Dan"))
    def envelope = new DocumentEnvelope(
        null,
        Meta.of(["myext:version": "1.0"]),
        null)
    def mapped = mapper.toMappedDocument(
        article,
        envelope,
        CompoundSerializationContext.defaults()
        .withIncludePaths(List.of(IncludePath.of("author")))
        .withIncludePolicy(IncludePolicy.allowAll())
        .withFieldsets([articles: ["title"]]))
    def base = new ValidationContext(
        DocumentUsage.RESPONSE_OR_OTHER,
        Set.of("myext"),
        Set.of(),
        Set.of(),
        Set.of(),
        LinksContext.TOP_LEVEL,
        Map.of(),
        null)

    when:
    JsonApiJackson3.writer(jackson, base).writeValueAsString(mapped)

    then:
    noExceptionThrown()

    when:
    JsonApiJackson3.writer(jackson).writeValueAsString(mapped)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.DISALLOWED_ADDITIONAL_MEMBER
  }

  def "mapped writing unions bound and mapped linkage exemptions"() {
    given:
    def article = ResourceObject.of("articles", "1")
    def boundOrphan = ResourceObject.of("people", "9")
    def mappedOrphan = ResourceObject.of("people", "10")
    def mapped = new MappedDocument(
        new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null, null, null, null,
        [boundOrphan, mappedOrphan],
        [:]),
        Set.of(ResourceIdentity.ofId("people", "10")))
    def base = ValidationContext.defaults()
        .withSparseFieldsetLinkageExemptions(Set.of(ResourceIdentity.ofId("people", "9")))

    when:
    JsonApiJackson3.writer(jackson, base).writeValueAsString(mapped)

    then:
    noExceptionThrown()
  }

  def "exempted sparse-fieldset orphans stay valid while unrelated full-linkage defects fail"() {
    given:
    def fieldsetOrphanArticle = ResourceObject.of("articles", "1")
    def exemptedAuthor = ResourceObject.of("people", "9")
    def unrelatedOrphan = ResourceObject.of("tags", "7")
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(fieldsetOrphanArticle),
        null, null, null, null,
        [
          exemptedAuthor,
          unrelatedOrphan
        ],
        [:])
    def mapped = new MappedDocument(
        document, Set.of(ResourceIdentity.ofId("people", "9")))

    when:
    JsonApiJackson3.writer(jackson).writeValueAsString(mapped)

    then:
    def ex = thrown(JsonApiValidationException)
    ex.ruleCode() == ValidationRuleCode.FULL_LINKAGE_VIOLATION

    when:
    def subtreeAuthor = new ResourceObject(
        "people", "9", null, null,
        Relationships.ofRelationships([
          editor: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "10")))
        ]),
        null, null, [:])
    def childOfExempted = ResourceObject.of("people", "10")
    def subtreeDocument = new JsonApiDocument(
        new DocumentData.SingleResource(fieldsetOrphanArticle),
        null, null, null, null,
        [
          subtreeAuthor,
          childOfExempted
        ],
        [:])
    JsonApiJackson3.writer(jackson).writeValueAsString(
        new MappedDocument(subtreeDocument, Set.of(ResourceIdentity.ofId("people", "9"))))

    then:
    noExceptionThrown()
  }

  def "all output variants compose provenance consistently"() {
    given:
    def scenario = SparseFieldsetScenarios.catalog().byId(
        "include author with fields articles title omits linkage and sets exception")
    def request = (SparseFieldsetRequest.Single) scenario.request()
    def mapped = mapper.toMappedDocument(request.supplier().get(), null, request.context())
    def writer = JsonApiJackson3.writer(jackson)
    def manualWriter = JsonApiJackson3.writer(
        jackson,
        ValidationContext.defaults()
        .withSparseFieldsetLinkageExemptions(mapped.sparseFieldsetLinkageExemptions()))

    expect:
    def composedJson = writer.writeValueAsString(mapped)
    composedJson == manualWriter.writeValueAsString(mapped.document())
    writer.writeValueAsBytes(mapped) ==
        composedJson.getBytes(StandardCharsets.UTF_8)
    with(new ByteArrayOutputStream()) { stream ->
      writer.writeValue(stream, mapped)
      stream.toByteArray() == writer.writeValueAsBytes(mapped)
    }
    with(new StringWriter()) { out ->
      writer.writeValue(out, mapped)
      out.toString() == composedJson
    }
    with(new ByteArrayOutputStream()) { stream ->
      def generator = jackson.createGenerator(stream)
      writer.writeValue(generator, mapped)
      generator.close()
      stream.toByteArray() == writer.writeValueAsBytes(mapped)
    }
  }

  def "no-provenance mappings write under the unchanged base context"() {
    given:
    def scenario = SparseFieldsetScenarios.catalog().byId(
        "full field list keeps the unrestricted resource state")
    def request = (SparseFieldsetRequest.Single) scenario.request()
    def mapped = mapper.toMappedDocument(request.supplier().get(), null, request.context())

    expect:
    mapped.sparseFieldsetLinkageExemptions().isEmpty()
    JsonApiJackson3.writer(jackson).writeValueAsString(mapped) ==
        JsonApiJackson3.writer(jackson).writeValueAsString(mapped.document())
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
      def supplied = request.supplier().get()
      assert supplied instanceof AccessCountingFieldsetArticle,
      "zeroReads expectations require AccessCountingFieldsetArticle: " + supplied?.class
      def counting = (AccessCountingFieldsetArticle) supplied
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
    assert mapped.sparseFieldsetLinkageExemptions().isEmpty() == !success.expectsLinkageExemptions()
    def resource = primaryResource(mapped.document().data())
    assertResource(success.primary(), resource)
    assertIncluded(mapped.document().included(), success.included())
  }

  private static ResourceObject primaryResource(def data) {
    if (data instanceof DocumentData.SingleResource) {
      return data.resource()
    }
    if (data instanceof DocumentData.ResourceCollection) {
      assert data.resources().size() == 1
      return data.resources()[0]
    }
    throw new IllegalArgumentException("Unsupported primary data: " + data)
  }

  private static void assertResource(FieldsetResourceState expected, ResourceObject actual) {
    expected.assertMatches(actual)
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
