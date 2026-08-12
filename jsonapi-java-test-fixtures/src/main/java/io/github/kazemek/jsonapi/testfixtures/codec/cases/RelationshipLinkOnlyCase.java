package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class RelationshipLinkOnlyCase {

  private RelationshipLinkOnlyCase() {}

  public static CodecFixture fixture() {
    Map<String, @Nullable Link> authorLinkEntries = new LinkedHashMap<>();
    authorLinkEntries.put(
        "self", Models.stringLink("http://example.com/articles/1/relationships/author"));
    authorLinkEntries.put("related", Models.stringLink("http://example.com/articles/1/author"));
    var authorLinks = Models.links(authorLinkEntries);
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put("author", Relationship.linkOnly(authorLinks));
    var article = Models.resource("articles", "1", Relationships.ofRelationships(relationships));
    return CodecFixture.of(
        "relationship-link-only",
        "Link-only relationship without data",
        "documents/relationship-link-only.json",
        JsonApiDocument.withData(new DocumentData.SingleResource(article)),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
