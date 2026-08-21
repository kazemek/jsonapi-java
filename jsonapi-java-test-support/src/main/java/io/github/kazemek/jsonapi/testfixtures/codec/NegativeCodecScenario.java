package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.testfixtures.Scenario;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * One read-only negative codec case: a wire input that must fail to decode, with the
 * version-neutral expected failure category, JSON Pointer, and core rule code (each recorded only
 * when present). Category and rule-code values are manifest strings so no Jackson-major adapter
 * types leak into the shared corpus.
 */
public record NegativeCodecScenario(
    String id,
    String notes,
    String path,
    String category,
    @Nullable String pointer,
    @Nullable String ruleCode,
    boolean sourceLocation)
    implements Scenario {

  public NegativeCodecScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(notes, "notes");
    Objects.requireNonNull(path, "path");
    Objects.requireNonNull(category, "category");
  }

  @Override
  public String toString() {
    return id;
  }
}
