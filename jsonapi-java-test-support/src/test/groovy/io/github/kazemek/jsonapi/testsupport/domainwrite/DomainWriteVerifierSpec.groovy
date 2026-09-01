package io.github.kazemek.jsonapi.testsupport.domainwrite

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.DocumentData
import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import io.github.kazemek.jsonapi.jackson.document.DocumentEnvelope
import spock.lang.Specification

class DomainWriteVerifierSpec extends Specification {

  def "resource success compares identity, attributes, resource meta, and relationship meta"() {
    given:
    def extra = ["ext:href": "https://example.test/p1"]
    def expected = resource(
        "articles",
        "1",
        "lid-1",
        Attributes.ofAttributes([title: "T"]),
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.SingleLinkage(
          new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), extra)),
          null,
          Meta.of([displayName: "Alice"]),
          [:])]),
        Meta.of([source: "cms"]))
    def actual = resource(
        "articles",
        "1",
        "lid-1",
        Attributes.ofAttributes([title: "T"]),
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.SingleLinkage(
          new ResourceIdentifier("people", "p1", null, Meta.of([role: "editor"]), extra)),
          null,
          Meta.of([displayName: "Alice"]),
          [:])]),
        Meta.of([source: "cms"]))
    def scenario = resourceScenario("meta", expected)

    when:
    DomainWriteVerifier.verify(scenario, actual, null)

    then:
    noExceptionThrown()
  }

  def "unordered to-many linkage compares identifier sets and still checks relationship meta"() {
    given:
    def first = ResourceIdentifier.of("tags", "java")
    def second = ResourceIdentifier.of("tags", "groovy")
    def expected = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [tags: new Relationship(
          new RelationshipData.IdentifierCollectionLinkage([first, second]),
          null,
          Meta.of([status: "open"]),
          [:])]),
        null)
    def actual = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [tags: new Relationship(
          new RelationshipData.IdentifierCollectionLinkage([second, first]),
          null,
          Meta.of([status: "open"]),
          [:])]),
        null)
    def scenario = new DomainWriteScenario(
        "unordered",
        DomainWriteOperation.TO_RESOURCE,
        ignoredInput(),
        null,
        DomainWriteOutcome.resource(expected),
        unorderedTags())

    when:
    DomainWriteVerifier.verify(scenario, actual, null)

    then:
    noExceptionThrown()
  }

  def "document success compares envelope members and absent included"() {
    given:
    def resource = resource("articles", "1", null, null, null, null)
    def expected = new JsonApiDocument(
        new DocumentData.SingleResource(resource),
        null,
        Meta.of([key: "value"]),
        null,
        null,
        null,
        [:])
    def scenario = new DomainWriteScenario(
        "envelope",
        DomainWriteOperation.TO_DOCUMENT_WITH_ENVELOPE,
        ignoredInput(),
        new DocumentEnvelope(null, Meta.of([key: "value"]), null),
        DomainWriteOutcome.document(expected),
        DomainWriteComparisonPolicy.ordered())

    when:
    DomainWriteVerifier.verify(scenario, expected, null)

    then:
    noExceptionThrown()
  }

  def "resource collection success compares each resource in order"() {
    given:
    def first = resource("articles", "1", null, null, null, null)
    def second = resource("articles", "2", null, null, null, null)
    def document = new JsonApiDocument(
        new DocumentData.ResourceCollection([first, second]),
        null, null, null, null, null, [:])
    def scenario = new DomainWriteScenario(
        "collection",
        DomainWriteOperation.TO_RESOURCE_COLLECTION,
        emptyCollectionInput(),
        null,
        DomainWriteOutcome.document(document),
        DomainWriteComparisonPolicy.ordered())

    when:
    DomainWriteVerifier.verify(scenario, document, null)

    then:
    noExceptionThrown()
  }

  def "failure outcome requires the expected exception type"() {
    given:
    def scenario = new DomainWriteScenario(
        "null-input",
        DomainWriteOperation.TO_RESOURCE,
        nullInput(),
        null,
        DomainWriteOutcome.failure(NullPointerException),
        DomainWriteComparisonPolicy.ordered())

    when:
    DomainWriteVerifier.verify(scenario, null, new NullPointerException("x"))

    then:
    noExceptionThrown()

    when:
    DomainWriteVerifier.verify(scenario, "nope", null)

    then:
    def missing = thrown(AssertionError)
    missing.message.contains("expected java.lang.NullPointerException")

    when:
    DomainWriteVerifier.verify(scenario, null, new IllegalStateException("x"))

    then:
    def wrong = thrown(AssertionError)
    wrong.message.contains("IllegalStateException")
  }

  def "relationship meta mismatch fails even when linkage matches"() {
    given:
    def identifier = ResourceIdentifier.of("people", "p1")
    def expected = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.SingleLinkage(identifier),
          null,
          Meta.of([displayName: "Alice"]),
          [:])]),
        null)
    def actual = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.SingleLinkage(identifier),
          null,
          null,
          [:])]),
        null)
    def scenario = resourceScenario("rel-meta", expected)

    when:
    DomainWriteVerifier.verify(scenario, actual, null)

    then:
    def ex = thrown(AssertionError)
    ex.message.contains("author.meta")
  }

  def "NullLinkage mismatch is reported"() {
    given:
    def expected = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(RelationshipData.NullLinkage.INSTANCE, null, null, [:])]),
        null)
    def actual = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "p1")),
          null, null, [:])]),
        null)
    def scenario = resourceScenario("null-link", expected)

    when:
    DomainWriteVerifier.verify(scenario, actual, null)

    then:
    thrown(AssertionError)
  }

  def "failure without a throwable and unexpected throwables are rejected"() {
    given:
    def failure = new DomainWriteScenario(
        "null-input",
        DomainWriteOperation.TO_RESOURCE,
        nullInput(),
        null,
        DomainWriteOutcome.failure(NullPointerException),
        DomainWriteComparisonPolicy.ordered())
    def success = resourceScenario("ok", resource("articles", "1", null, null, null, null))

    when:
    DomainWriteVerifier.verify(failure, "ignored", null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(
        success, resource("articles", "1", null, null, null, null), new IllegalStateException("x"))

    then:
    thrown(AssertionError)
  }

  def "success rejects a result of the wrong document shape"() {
    given:
    def article = resource("articles", "1", null, null, null, null)
    def toResourceScenario = resourceScenario("resource-shape", article)
    def document = new JsonApiDocument(
        new DocumentData.SingleResource(article),
        null, Meta.of([k: "v"]), null, null, null, [:])
    def documentScenario = new DomainWriteScenario(
        "document-shape",
        DomainWriteOperation.TO_DOCUMENT,
        ignoredInput(),
        null,
        DomainWriteOutcome.document(document),
        DomainWriteComparisonPolicy.ordered())
    def identifierDocument = new JsonApiDocument(
        new DocumentData.SingleIdentifier(ResourceIdentifier.of("articles", "1")),
        null, null, null, null, null, [:])
    def identifierScenario = new DomainWriteScenario(
        "identifier-data",
        DomainWriteOperation.TO_DOCUMENT,
        ignoredInput(),
        null,
        DomainWriteOutcome.document(identifierDocument),
        DomainWriteComparisonPolicy.ordered())
    def collection = new JsonApiDocument(
        new DocumentData.ResourceCollection([article]),
        null, null, null, null, null, [:])
    def collectionScenario = new DomainWriteScenario(
        "collection-shape",
        DomainWriteOperation.TO_RESOURCE_COLLECTION,
        emptyCollectionInput(),
        null,
        DomainWriteOutcome.document(collection),
        DomainWriteComparisonPolicy.ordered())
    def two = new JsonApiDocument(
        new DocumentData.ResourceCollection(
        [
          article,
          resource("articles", "2", null, null, null, null)
        ]),
        null, null, null, null, null, [:])
    def absentData = new JsonApiDocument(null, null, Meta.of([k: "v"]), null, null, null, [:])
    def absentScenario = new DomainWriteScenario(
        "absent-data",
        DomainWriteOperation.TO_DOCUMENT,
        ignoredInput(),
        null,
        DomainWriteOutcome.document(absentData),
        DomainWriteComparisonPolicy.ordered())

    when:
    DomainWriteVerifier.verify(toResourceScenario, "not a resource", null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(documentScenario, "not a document", null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(documentScenario, collection, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(collectionScenario, document, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(collectionScenario, two, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(identifierScenario, identifierDocument, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(absentScenario, document, null)

    then:
    thrown(AssertionError)
  }

  def "attributes, relationships, and linkage absences and type mismatches fail"() {
    given:
    def identifier = ResourceIdentifier.of("people", "p1")
    def withAttributes = resource(
        "articles", "1", null, Attributes.ofAttributes([title: "T"]), null, null)
    def withoutAttributes = resource("articles", "1", null, null, null, null)
    def withRel = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(new RelationshipData.SingleLinkage(identifier), null, null, [:])]),
        null)
    def withoutRel = resource("articles", "1", null, null, null, null)
    def nullLink = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(RelationshipData.NullLinkage.INSTANCE, null, null, [:])]),
        null)
    def toMany = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.IdentifierCollectionLinkage([identifier]),
          null, null, [:])]),
        null)
    def emptyToMany = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: new Relationship(
          new RelationshipData.IdentifierCollectionLinkage([]),
          null, null, [:])]),
        null)
    def first = ResourceIdentifier.of("tags", "java")
    def second = ResourceIdentifier.of("tags", "groovy")
    def unorderedExpected = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [tags: new Relationship(
          new RelationshipData.IdentifierCollectionLinkage([first, second]),
          null, null, [:])]),
        null)
    def unorderedActual = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [tags: new Relationship(
          new RelationshipData.IdentifierCollectionLinkage(
          [
            ResourceIdentifier.of("tags", "other"),
            first
          ]),
          null, null, [:])]),
        null)
    def dataLessRel = resource(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships([author: Relationship.metaOnly(Meta.of([k: "v"]))]),
        null)

    when:
    DomainWriteVerifier.verify(resourceScenario("attrs", withAttributes), withoutAttributes, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(resourceScenario("attrs-absent", withoutAttributes), withAttributes, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(resourceScenario("rels", withRel), withoutRel, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(resourceScenario("rels-absent", withoutRel), withRel, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(resourceScenario("single", withRel), toMany, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(resourceScenario("to-many", toMany), withRel, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(resourceScenario("to-many-size", toMany), emptyToMany, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(resourceScenario("null-vs-absent", nullLink), dataLessRel, null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(
        new DomainWriteScenario(
        "unordered-mismatch",
        DomainWriteOperation.TO_RESOURCE,
        ignoredInput(),
        null,
        DomainWriteOutcome.resource(unorderedExpected),
        unorderedTags()),
        unorderedActual,
        null)

    then:
    thrown(AssertionError)

    when:
    DomainWriteVerifier.verify(
        resourceScenario("ordered-mismatch", unorderedExpected), unorderedActual, null)

    then:
    thrown(AssertionError)
  }

  def "included resources apply unordered identifier policy without reordering included itself"() {
    given:
    def first = ResourceIdentifier.of("tags", "java")
    def second = ResourceIdentifier.of("tags", "groovy")
    def primary = resource("articles", "1", null, null, null, null)
    def expectedIncluded = taggedPerson(first, second)
    def actualIncluded = taggedPerson(second, first)
    def expected = documentWithIncluded(primary, expectedIncluded)
    def actual = documentWithIncluded(primary, actualIncluded)
    def scenario = includedScenario("included-unordered", expected)

    when:
    DomainWriteVerifier.verify(scenario, actual, null)

    then:
    noExceptionThrown()
  }

  def "included resource mismatch still fails"() {
    given:
    def first = ResourceIdentifier.of("tags", "java")
    def second = ResourceIdentifier.of("tags", "groovy")
    def other = ResourceIdentifier.of("tags", "other")
    def primary = resource("articles", "1", null, null, null, null)
    def expected = documentWithIncluded(primary, taggedPerson(first, second))
    def actual = documentWithIncluded(primary, taggedPerson(other, first))
    def scenario = includedScenario("included-mismatch", expected)

    when:
    DomainWriteVerifier.verify(scenario, actual, null)

    then:
    thrown(AssertionError)
  }

  private static DomainWriteInput.SingleInput ignoredInput() {
    return new DomainWriteInput.SingleInput({ "ignored" })
  }

  private static DomainWriteInput.SingleInput nullInput() {
    return new DomainWriteInput.SingleInput({ null })
  }

  private static DomainWriteInput.CollectionInput emptyCollectionInput() {
    return new DomainWriteInput.CollectionInput({ List.of() })
  }

  private static DomainWriteComparisonPolicy unorderedTags() {
    return new DomainWriteComparisonPolicy(
        Map.of("tags", DomainWriteComparisonPolicy.ComparisonOrder.UNORDERED_IDENTIFIER_PAIRS))
  }

  private static DomainWriteScenario resourceScenario(String id, ResourceObject expected) {
    return new DomainWriteScenario(
        id,
        DomainWriteOperation.TO_RESOURCE,
        ignoredInput(),
        null,
        DomainWriteOutcome.resource(expected),
        DomainWriteComparisonPolicy.ordered())
  }

  private static DomainWriteScenario includedScenario(String id, JsonApiDocument expected) {
    return new DomainWriteScenario(
        id,
        DomainWriteOperation.TO_DOCUMENT,
        ignoredInput(),
        null,
        DomainWriteOutcome.document(expected),
        unorderedTags())
  }

  private static JsonApiDocument documentWithIncluded(
      ResourceObject primary, ResourceObject included) {
    return new JsonApiDocument(
        new DocumentData.SingleResource(primary),
        null, null, null, null, [included], [:])
  }

  private static ResourceObject taggedPerson(
      ResourceIdentifier first, ResourceIdentifier second) {
    return resource(
        "people",
        "p1",
        null,
        null,
        Relationships.ofRelationships(
        [tags: new Relationship(
          new RelationshipData.IdentifierCollectionLinkage([first, second]),
          null, null, [:])]),
        null)
  }

  private static ResourceObject resource(
      String type,
      String id,
      String lid,
      Attributes attributes,
      Relationships relationships,
      Meta meta) {
    return new ResourceObject(type, id, lid, attributes, relationships, null, meta, [:])
  }
}
