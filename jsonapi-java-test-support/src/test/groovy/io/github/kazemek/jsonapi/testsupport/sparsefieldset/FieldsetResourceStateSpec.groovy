package io.github.kazemek.jsonapi.testsupport.sparsefieldset

import io.github.kazemek.jsonapi.core.model.Attributes
import io.github.kazemek.jsonapi.core.model.Meta
import io.github.kazemek.jsonapi.core.model.Relationship
import io.github.kazemek.jsonapi.core.model.RelationshipData
import io.github.kazemek.jsonapi.core.model.Relationships
import io.github.kazemek.jsonapi.core.model.ResourceIdentifier
import io.github.kazemek.jsonapi.core.model.ResourceObject
import spock.lang.Specification

class FieldsetResourceStateSpec extends Specification {

  def "assertMatches accepts identity, surviving fields, linkage, and resource meta"() {
    given:
    def expected = FieldsetResourceState.of(
        "articles",
        "1",
        ["title"],
        [title: "T"],
        ["author"],
        [author: new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "p1"))],
        Meta.of([source: "cms"]))
    def actual = new ResourceObject(
        "articles",
        "1",
        null,
        Attributes.ofAttributes([title: "T"]),
        Relationships.ofRelationships(
        [author: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "p1")))]),
        null,
        Meta.of([source: "cms"]),
        [:])

    when:
    expected.assertMatches(actual)

    then:
    noExceptionThrown()
  }

  def "assertMatches rejects a meta mismatch"() {
    given:
    def expected = FieldsetResourceState.of(
        "articles", "1", null, [:], null, [:], Meta.of([source: "cms"]))
    def actual = ResourceObject.of("articles", "1")

    when:
    expected.assertMatches(actual)

    then:
    thrown(AssertionError)
  }

  def "assertMatches treats a null attributeNames as absent attributes"() {
    given:
    def expected = FieldsetResourceState.identity("articles", "1")
    def withAttributes = new ResourceObject(
        "articles",
        "1",
        null,
        Attributes.ofAttributes([title: "T"]),
        null,
        null,
        null,
        [:])

    when:
    expected.assertMatches(withAttributes)

    then:
    thrown(AssertionError)
  }

  def "assertMatches rejects missing attributes, relationships, and linkage"() {
    given:
    def expectedAttributes = FieldsetResourceState.of(
        "articles", "1", ["title"], [title: "T"], null, [:])
    def expectedRelationships = FieldsetResourceState.of(
        "articles",
        "1",
        null,
        [:],
        ["author"],
        [author: new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "p1"))])
    def expectedAbsentRelationships = FieldsetResourceState.identity("articles", "1")
    def withRelationships = new ResourceObject(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [author: Relationship.withData(
          new RelationshipData.SingleLinkage(ResourceIdentifier.of("people", "p1")))]),
        null,
        null,
        [:])
    def identity = ResourceObject.of("articles", "1")
    def missingAuthor = new ResourceObject(
        "articles",
        "1",
        null,
        null,
        Relationships.ofRelationships(
        [comments: Relationship.withData(
          new RelationshipData.IdentifierCollectionLinkage([]))]),
        null,
        null,
        [:])

    when:
    expectedAttributes.assertMatches(identity)

    then:
    thrown(AssertionError)

    when:
    expectedRelationships.assertMatches(identity)

    then:
    thrown(AssertionError)

    when:
    expectedAbsentRelationships.assertMatches(withRelationships)

    then:
    thrown(AssertionError)

    when:
    expectedRelationships.assertMatches(missingAuthor)

    then:
    thrown(AssertionError)
  }
}
