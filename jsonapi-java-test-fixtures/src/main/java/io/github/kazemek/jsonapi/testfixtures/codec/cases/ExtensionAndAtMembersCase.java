package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaDisagreement;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ExtensionAndAtMembersCase {

  private ExtensionAndAtMembersCase() {}

  public static CodecFixture fixture() {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("title", "Hello");
    Map<String, Object> additionalMembers = new LinkedHashMap<>();
    additionalMembers.put("@copyright", "Copyright 2026");
    additionalMembers.put("ext:version", 1);
    var article =
        Models.resource("articles", "1", Attributes.ofAttributes(attributes), additionalMembers);

    Map<String, Object> documentMembers = new LinkedHashMap<>();
    documentMembers.put("ext:request-id", "abc-123");

    return new CodecFixture(
        "extension-and-at-members",
        "Extension and @ members on document and resource",
        "documents/extension-and-at-members.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            null,
            null,
            null,
            null,
            documentMembers),
        Models.extContext(),
        true,
        true,
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE,
        new SchemaDisagreement(
            "top-level ext: member; PR json-api/json-api#1603 does not yet model extension members (see its description)",
            List.of(Map.of("keyword", "unevaluatedProperties", "path", ""))),
        false,
        null,
        false);
  }
}
