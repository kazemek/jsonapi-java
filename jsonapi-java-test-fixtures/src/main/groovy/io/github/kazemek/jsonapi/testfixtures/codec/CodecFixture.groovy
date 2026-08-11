package io.github.kazemek.jsonapi.testfixtures.codec

import java.util.Objects

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind

/**
 * One canonical codec fixture: core model + validation context + expected wire JSON path, tagged
 * with the codec capabilities the fixture may exercise (write, read, schema kind, primary-data
 * kind, exact-byte policy, canonical hreflang, and known draft-schema disagreement).
 *
 * <p>The fixture ids and expected JSON paths are stable across Jackson majors; adapter tests
 * select cases by capability instead of maintaining independent hard-coded id lists.
 */
final class CodecFixture {
  final String id
  final String notes
  final String expectedPath
  final JsonApiDocument document
  final ValidationContext context
  final boolean writable
  final boolean readable
  final PrimaryDataKind primaryDataKind
  final SchemaKind schemaKind
  final SchemaDisagreement schemaDisagreement
  final boolean assertExactUtf8
  final String exactUtf8Path
  final boolean assertHreflangArray

  private CodecFixture(
  String id,
  String notes,
  String expectedPath,
  JsonApiDocument document,
  ValidationContext context,
  boolean writable,
  boolean readable,
  PrimaryDataKind primaryDataKind,
  SchemaKind schemaKind,
  SchemaDisagreement schemaDisagreement,
  boolean assertExactUtf8,
  String exactUtf8Path,
  boolean assertHreflangArray) {
    this.id = Objects.requireNonNull(id, "id")
    this.notes = Objects.requireNonNull(notes, "notes")
    this.expectedPath = Objects.requireNonNull(expectedPath, "expectedPath")
    this.document = Objects.requireNonNull(document, "document")
    this.context = Objects.requireNonNull(context, "context")
    this.writable = writable
    this.readable = readable
    this.primaryDataKind = primaryDataKind
    this.schemaKind = schemaKind
    this.schemaDisagreement = schemaDisagreement
    this.assertExactUtf8 = assertExactUtf8
    this.exactUtf8Path = exactUtf8Path
    this.assertHreflangArray = assertHreflangArray
  }

  /**
   * Named-argument factory. {@code primaryDataKind} is null when the document has no primary data
   * (or only kind-neutral data such as an explicit {@code null}), so read tests fall back to
   * {@link PrimaryDataKind#RESOURCE}. {@code schemaKind} is null when the fixture is not checked
   * against the draft schemas; {@code schemaDisagreement} documents a known draft gap and requires
   * {@code schemaKind}.
   */
  static CodecFixture of(Map args) {
    def schemaKind = args.schemaKind as SchemaKind
    def schemaDisagreement = args.schemaDisagreement as SchemaDisagreement
    if (schemaDisagreement != null && schemaKind == null) {
      throw new IllegalArgumentException('schemaDisagreement requires schemaKind')
    }
    return new CodecFixture(
        args.id as String,
        args.notes as String,
        args.expectedPath as String,
        args.document as JsonApiDocument,
        (args.context ?: ValidationContext.defaults()) as ValidationContext,
        (args.writable == null ? true : args.writable) as boolean,
        (args.readable == null ? true : args.readable) as boolean,
        args.primaryDataKind as PrimaryDataKind,
        schemaKind,
        schemaDisagreement,
        (args.assertExactUtf8 ?: false) as boolean,
        args.exactUtf8Path as String,
        (args.assertHreflangArray ?: false) as boolean)
  }

  @Override
  String toString() {
    return id
  }
}
