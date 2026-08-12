package io.github.kazemek.jsonapi.testfixtures.codec.cases;

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
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class LocalIdentifierCase {

  private LocalIdentifierCase() {}

  public static CodecFixture fixture() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.withLid("people", "temp-author"))));
    var article =
        Models.resourceWithLid("articles", "temp-1", Relationships.ofRelationships(relationships));
    return new CodecFixture(
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
