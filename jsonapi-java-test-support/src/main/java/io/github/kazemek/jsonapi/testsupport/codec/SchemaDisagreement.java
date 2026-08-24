package io.github.kazemek.jsonapi.testsupport.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Documented disagreement between a fixture and the pinned JSON:API 1.1 draft-PR schemas. The
 * fixture must keep failing the draft for the recorded reason, so a schema fix forces an
 * intentional re-review; a schema result never changes a conformance status.
 */
public record SchemaDisagreement(String reason, List<Map<String, String>> expected) {

  public SchemaDisagreement {
    Objects.requireNonNull(reason, "reason");
    List<Map<String, String>> copy = new ArrayList<>();
    for (Map<String, String> entry : Objects.requireNonNull(expected, "expected")) {
      copy.add(Map.copyOf(entry));
    }
    expected = List.copyOf(copy);
  }
}
