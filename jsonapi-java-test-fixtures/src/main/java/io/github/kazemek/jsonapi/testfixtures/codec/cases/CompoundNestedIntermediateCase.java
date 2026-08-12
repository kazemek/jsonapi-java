package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class CompoundNestedIntermediateCase {

  private static final String TYPE_ARTICLES = "articles";
  private static final String TYPE_COMMENTS = "comments";
  private static final String TYPE_PEOPLE = "people";

  private CompoundNestedIntermediateCase() {}

  public static CodecFixture fixture() {
    Map<String, @Nullable Relationship> articleRelationships = new LinkedHashMap<>();
    articleRelationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, "9"))));
    articleRelationships.put(
        "comments",
        Relationship.withData(
            new RelationshipData.IdentifierCollectionLinkage(
                List.of(
                    Models.identifier(TYPE_COMMENTS, "5"),
                    Models.identifier(TYPE_COMMENTS, "12")))));
    var article =
        Models.resource(TYPE_ARTICLES, "1", Relationships.ofRelationships(articleRelationships));

    Map<String, @Nullable Relationship> comment5Relationships = new LinkedHashMap<>();
    comment5Relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, "2"))));
    var comment5 =
        Models.resource(
            TYPE_COMMENTS,
            "5",
            Attributes.ofAttributes(attribute("body", "First!")),
            Relationships.ofRelationships(comment5Relationships));

    var person2 =
        Models.resource(TYPE_PEOPLE, "2", Attributes.ofAttributes(attribute("name", "Ezra")));

    Map<String, @Nullable Relationship> comment12Relationships = new LinkedHashMap<>();
    comment12Relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, "9"))));
    var comment12 =
        Models.resource(
            TYPE_COMMENTS,
            "12",
            Attributes.ofAttributes(attribute("body", "I like XML better")),
            Relationships.ofRelationships(comment12Relationships));

    var person9 =
        Models.resource(TYPE_PEOPLE, "9", Attributes.ofAttributes(attribute("name", "Dan")));

    return CodecFixture.of(
        "compound-nested-intermediate",
        "Compound document with nested comments.author intermediates",
        "documents/compound-nested-intermediate.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            null,
            null,
            null,
            List.of(comment5, comment12, person2, person9),
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static Map<String, Object> attribute(String name, String value) {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put(name, value);
    return attributes;
  }
}
