package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

final class CompoundSharedIdentityScenario {

  private static final String TYPE_ARTICLES = "articles";
  private static final String TYPE_PEOPLE = "people";

  private CompoundSharedIdentityScenario() {}

  public static CodecScenario scenario() {
    var article1 = Models.resource(TYPE_ARTICLES, "1", authorRelationship());
    var article2 = Models.resource(TYPE_ARTICLES, "2", authorRelationship());
    Map<String, Object> includedAttributes = new LinkedHashMap<>();
    includedAttributes.put("name", "Dan");
    var included = Models.resource(TYPE_PEOPLE, "9", Attributes.ofAttributes(includedAttributes));
    return CodecScenario.of(
        "compound-shared-identity",
        "Compound collection sharing one included author identity",
        "documents/compound-shared-identity.json",
        new JsonApiDocument(
            new DocumentData.ResourceCollection(List.of(article1, article2)),
            null,
            null,
            null,
            null,
            List.of(included),
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static Relationships authorRelationship() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.identifier(TYPE_PEOPLE, "9"))));
    return Relationships.ofRelationships(relationships);
  }
}
