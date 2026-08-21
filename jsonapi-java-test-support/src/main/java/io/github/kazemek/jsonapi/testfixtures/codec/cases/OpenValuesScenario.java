package io.github.kazemek.jsonapi.testfixtures.codec.cases;

import io.github.kazemek.jsonapi.core.model.Attributes;
import io.github.kazemek.jsonapi.core.model.DocumentData;
import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.model.Meta;
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.Models;
import io.github.kazemek.jsonapi.testfixtures.codec.SchemaKind;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

public final class OpenValuesScenario {

  private OpenValuesScenario() {}

  public static CodecScenario scenario() {
    Map<String, @Nullable Object> nested = new LinkedHashMap<>();
    nested.put("tags", List.of("a", "b"));
    Map<String, Object> counts = new LinkedHashMap<>();
    counts.put("views", 2);
    nested.put("counts", counts);

    Map<String, @Nullable Object> attributes = new LinkedHashMap<>();
    attributes.put("nullable", null);
    attributes.put("nested", nested);
    attributes.put("intValue", 42);
    attributes.put("longValue", 9007199254740991L);
    attributes.put("floatValue", 1.5f);
    attributes.put("doubleValue", 2.25d);
    attributes.put("bigIntValue", new BigInteger("123456789012345678901234567890"));
    attributes.put("bigDecimalValue", new BigDecimal("1234567890.123456789"));

    var article = Models.resource("articles", "1", Attributes.ofAttributes(attributes));

    Map<String, @Nullable Object> meta = new LinkedHashMap<>();
    meta.put("flag", true);
    meta.put("nullMeta", null);

    return CodecScenario.of(
        "open-values",
        "Open JSON null, nested object/array, and numeric families in attributes/meta",
        "documents/open-values.json",
        new JsonApiDocument(
            new DocumentData.SingleResource(article),
            null,
            Meta.of(meta),
            null,
            null,
            null,
            Map.of()),
        PrimaryDataKind.RESOURCE,
        SchemaKind.RESPONSE);
  }
}
