package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecFixture;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ResourceCollectionCase {

  private ResourceCollectionCase() {}

  public static CodecFixture fixture() {
    var first = Models.resource("articles", "1", Attributes.ofAttributes(title("First")));
    var second = Models.resource("articles", "2", Attributes.ofAttributes(title("Second")));
    return CodecFixture.of(
        "resource-collection",
        "Resource collection primary data",
        "documents/resource-collection.json",
        JsonApiDocument.withData(new DocumentData.ResourceCollection(List.of(first, second))),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }

  private static Map<String, Object> title(String value) {
    Map<String, Object> attributes = new LinkedHashMap<>();
    attributes.put("title", value);
    return attributes;
  }
}
