package io.github.kazemek.jsonapi.testsupport.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
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
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class CompoundDocumentScenario {

  private CompoundDocumentScenario() {}

  public static CodecScenario scenario() {
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.identifier("people", "9"))));
    var article = Models.resource("articles", "1", Relationships.ofRelationships(relationships));
    Map<String, Object> includedAttributes = new LinkedHashMap<>();
    includedAttributes.put("name", "Dan");
    var included = Models.resource("people", "9", Attributes.ofAttributes(includedAttributes));
    return CodecScenario.of(
        "compound-document",
        "Compound document with included resources",
        "documents/compound-document.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            null,
            null,
            null,
            List.of(included),
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
