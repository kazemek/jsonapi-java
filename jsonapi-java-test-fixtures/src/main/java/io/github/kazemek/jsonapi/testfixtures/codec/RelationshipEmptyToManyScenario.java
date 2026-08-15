package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

final class RelationshipEmptyToManyScenario {

  private RelationshipEmptyToManyScenario() {}

  public static CodecScenario scenario() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        "comments", Relationship.withData(RelationshipData.IdentifierCollectionLinkage.empty()));
    var article = Models.resource("articles", "1", Relationships.ofRelationships(relationships));
    return CodecScenario.of(
        "relationship-empty-to-many",
        "Empty to-many relationship data array",
        "documents/relationship-empty-to-many.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
