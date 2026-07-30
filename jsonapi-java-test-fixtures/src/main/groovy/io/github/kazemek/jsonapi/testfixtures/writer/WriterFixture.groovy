package io.github.kazemek.jsonapi.testfixtures.writer

import java.util.Objects

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.validation.ValidationContext

/**
 * One canonical writer fixture: core model + validation context + expected wire JSON path.
 */
final class WriterFixture {
  final String id
  final String notes
  final String expectedPath
  final JsonApiDocument document
  final ValidationContext context
  final boolean assertExactUtf8
  final String exactUtf8Path
  final boolean assertHreflangArray

  private WriterFixture(
  String id,
  String notes,
  String expectedPath,
  JsonApiDocument document,
  ValidationContext context,
  boolean assertExactUtf8,
  String exactUtf8Path,
  boolean assertHreflangArray) {
    this.id = Objects.requireNonNull(id, "id")
    this.notes = Objects.requireNonNull(notes, "notes")
    this.expectedPath = Objects.requireNonNull(expectedPath, "expectedPath")
    this.document = Objects.requireNonNull(document, "document")
    this.context = Objects.requireNonNull(context, "context")
    this.assertExactUtf8 = assertExactUtf8
    this.exactUtf8Path = exactUtf8Path
    this.assertHreflangArray = assertHreflangArray
  }

  static WriterFixture of(Map args) {
    return new WriterFixture(
        args.id as String,
        args.notes as String,
        args.expectedPath as String,
        args.document as JsonApiDocument,
        (args.context ?: ValidationContext.defaults()) as ValidationContext,
        (args.assertExactUtf8 ?: false) as boolean,
        args.exactUtf8Path as String,
        (args.assertHreflangArray ?: false) as boolean)
  }

  @Override
  String toString() {
    return id
  }
}
