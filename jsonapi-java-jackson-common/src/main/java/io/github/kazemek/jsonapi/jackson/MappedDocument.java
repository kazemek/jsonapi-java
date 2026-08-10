package io.github.kazemek.jsonapi.jackson;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import java.util.Objects;

/**
 * Domain-mapping result that carries the produced document plus whether any relationship was
 * omitted by an applied sparse fieldset during that mapping call.
 *
 * <p>Pass {@link #applyTo(ValidationContext)} into the codec writer factory so aggregate validation
 * can skip full linkage when relationships were fieldset-excluded while included resources were
 * still traversed.
 */
public record MappedDocument(JsonApiDocument document, boolean sparseFieldsetException) {

  public MappedDocument {
    Objects.requireNonNull(document, "document");
  }

  /**
   * Returns {@code base.withSparseFieldsetException(true)} when this mapping omitted at least one
   * relationship by fieldset; otherwise returns {@code base} unchanged.
   */
  public ValidationContext applyTo(ValidationContext base) {
    Objects.requireNonNull(base, "base");
    return sparseFieldsetException ? base.withSparseFieldsetException(true) : base;
  }
}
