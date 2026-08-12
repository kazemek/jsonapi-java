package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.JsonApiObject;
import io.github.kazemek.jsonapi.core.model.Link;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.core.model.Relationship;
import io.github.kazemek.jsonapi.core.model.RelationshipData;
import io.github.kazemek.jsonapi.core.model.Relationships;
import io.github.kazemek.jsonapi.core.model.ResourceObject;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaDisagreement;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class MemberOrderScenario {

  private MemberOrderScenario() {}

  public static CodecScenario scenario() {
    var self = Models.stringLink("http://example.com/articles/1");
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("title", "Ordered");
    Map<String, @Nullable Relationship> relationships = new LinkedHashMap<>();
    relationships.put(
        "author",
        Relationship.withData(
            new RelationshipData.SingleLinkage(Models.identifier("people", "9"))));
    Map<String, @Nullable Link> resourceLinks = new LinkedHashMap<>();
    resourceLinks.put("self", self);
    Map<String, Object> resourceMeta = new LinkedHashMap<>();
    resourceMeta.put("created", "2026-01-01");
    Map<String, Object> resourceMembers = new LinkedHashMap<>();
    resourceMembers.put("ext:flag", true);
    var article =
        Models.resource(
            "articles",
            "1",
            "temp-1",
            Attributes.ofAttributes(attributes),
            Relationships.ofRelationships(relationships),
            Models.links(resourceLinks),
            Meta.of(resourceMeta),
            resourceMembers);

    Map<String, Object> documentMeta = new LinkedHashMap<>();
    documentMeta.put("copyright", "Copyright 2026");
    Map<String, @Nullable Link> documentLinks = new LinkedHashMap<>();
    documentLinks.put("self", self);
    Map<String, Object> documentMembers = new LinkedHashMap<>();
    documentMembers.put("ext:trace", "t-1");

    return new CodecScenario(
        "member-order",
        "Canonical standard member order then additional members",
        "documents/member-order.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            Meta.of(documentMeta),
            JsonApiObject.ofVersion("1.1"),
            Models.links(documentLinks),
            List.of(ResourceObject.of("people", "9")),
            documentMembers),
        Models.extContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        new SchemaDisagreement(
            "response resource carries both id and lid and top-level ext: members; the draft schema requires id and forbids lid in response resources and only models @ members",
            List.of(
                Map.of("keyword", "not", "path", "/data"),
                Map.of("keyword", "unevaluatedProperties", "path", ""))),
        true,
        "documents/member-order.compact.json",
        false);
  }
}
