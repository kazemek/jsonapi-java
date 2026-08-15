package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

final class RelationshipMetaOnlyScenario {

  private RelationshipMetaOnlyScenario() {}

  public static CodecScenario scenario() {
    Map<String, Object> meta = new LinkedHashMap<>();
    meta.put("inferred", true);
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put("author", Relationship.metaOnly(Meta.of(meta)));
    var article = Models.resource("articles", "1", Relationships.ofRelationships(relationships));
    return CodecScenario.of(
        "relationship-meta-only",
        "Meta-only relationship without data",
        "documents/relationship-meta-only.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
