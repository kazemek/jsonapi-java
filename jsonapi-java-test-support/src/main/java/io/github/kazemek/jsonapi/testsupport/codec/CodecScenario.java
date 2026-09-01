package io.github.kazemek.jsonapi.testsupport.codec;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.core.validation.ValidationContext;
import io.github.kazemek.jsonapi.jackson.document.PrimaryDataKind;
import io.github.kazemek.jsonapi.testsupport.Scenario;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * One canonical codec fixture: core model + validation context + expected wire JSON path, tagged
 * with the codec capabilities the fixture may exercise (write, read, schema kind, primary-data
 * kind, exact-byte policy, canonical hreflang, and known draft-schema disagreement).
 *
 * <p>The fixture ids and expected JSON paths are stable across Jackson majors; adapter tests select
 * cases by capability instead of maintaining independent hard-coded id lists. {@code
 * primaryDataKind} is null when the document has no primary data (or only kind-neutral data such as
 * an explicit {@code null}), so read tests fall back to {@link PrimaryDataKind#RESOURCE}. {@code
 * schemaKind} is null when the fixture is not checked against the draft schemas; {@code
 * schemaDisagreement} documents a known draft gap and requires {@code schemaKind}.
 */
public record CodecScenario(
    String id,
    String notes,
    String expectedPath,
    JsonApiDocument document,
    ValidationContext context,
    boolean writable,
    boolean readable,
    @Nullable PrimaryDataKind primaryDataKind,
    @Nullable SchemaKind schemaKind,
    @Nullable SchemaDisagreement schemaDisagreement,
    boolean assertExactUtf8,
    @Nullable String exactUtf8Path,
    boolean assertHreflangArray)
    implements Scenario {

  public CodecScenario {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(notes, "notes");
    Objects.requireNonNull(expectedPath, "expectedPath");
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(context, "context");
    if (schemaDisagreement != null && schemaKind == null) {
      throw new IllegalArgumentException("schemaDisagreement requires schemaKind");
    }
  }

  /**
   * Factory with the catalog defaults: {@link ValidationContext#defaults()}, {@code writable} and
   * {@code readable} true, no schema disagreement, and exact-UTF-8 / hreflang flags false.
   */
  public static CodecScenario of(
      String id,
      String notes,
      String expectedPath,
      JsonApiDocument document,
      @Nullable PrimaryDataKind primaryDataKind,
      @Nullable SchemaKind schemaKind) {
    return new CodecScenario(
        id,
        notes,
        expectedPath,
        document,
        ValidationContext.defaults(),
        true,
        true,
        primaryDataKind,
        schemaKind,
        null,
        false,
        null,
        false);
  }

  @Override
  public String toString() {
    return id;
  }
}
