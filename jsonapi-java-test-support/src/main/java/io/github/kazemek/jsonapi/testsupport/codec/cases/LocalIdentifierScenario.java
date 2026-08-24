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

public final class LocalIdentifierScenario {

  private LocalIdentifierScenario() {}

  public static CodecScenario scenario() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.withLid("people", "temp-author"))));
    var article =
        Models.resourceWithLid("articles", "temp-1", Relationships.ofRelationships(relationships));
    return new CodecScenario(
        "local-identifier",
        "Resource and linkage with lid",
        "documents/local-identifier.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        Models.createContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.CREATE,
        null,
        false,
        null,
        false);
  }
}
