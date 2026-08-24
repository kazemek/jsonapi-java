package io.github.kazemek.jsonapi.testsupport.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testsupport.codec.CodecScenario;
import io.github.kazemek.jsonapi.testsupport.codec.Models;
import io.github.kazemek.jsonapi.testsupport.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class RelationshipNullLinkageScenario {

  private RelationshipNullLinkageScenario() {}

  public static CodecScenario scenario() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put("author", Relationship.withData(RelationshipData.NullLinkage.INSTANCE));
    var article = Models.resource("articles", "1", Relationships.ofRelationships(relationships));
    return CodecScenario.of(
        "relationship-null-linkage",
        "Explicit null to-one relationship data",
        "documents/relationship-null-linkage.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
