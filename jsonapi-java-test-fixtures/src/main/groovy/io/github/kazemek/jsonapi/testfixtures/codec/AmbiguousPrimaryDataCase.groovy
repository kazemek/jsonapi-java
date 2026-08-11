package io.github.kazemek.jsonapi.testfixtures.codec

import java.util.Objects

import io.github.kazemek.jsonapi.core.model.JsonApiDocument
import io.github.kazemek.jsonapi.core.validation.ValidationContext
import io.github.kazemek.jsonapi.jackson.PrimaryDataKind

/**
 * One shared dual-success primary-data case: a wire document whose decoded model depends on the
 * explicit {@link PrimaryDataKind}, plus the expected model under both kinds. Such cases must
 * never be classified as failure fixtures.
 */
final class AmbiguousPrimaryDataCase {
  final String id
  final String notes
  final String expectedPath
  final JsonApiDocument resourceDocument
  final JsonApiDocument identifierDocument
  final ValidationContext context

  private AmbiguousPrimaryDataCase(
  String id,
  String notes,
  String expectedPath,
  JsonApiDocument resourceDocument,
  JsonApiDocument identifierDocument,
  ValidationContext context) {
    this.id = Objects.requireNonNull(id, "id")
    this.notes = Objects.requireNonNull(notes, "notes")
    this.expectedPath = Objects.requireNonNull(expectedPath, "expectedPath")
    this.resourceDocument = Objects.requireNonNull(resourceDocument, "resourceDocument")
    this.identifierDocument = Objects.requireNonNull(identifierDocument, "identifierDocument")
    this.context = Objects.requireNonNull(context, "context")
  }

  /** Named-argument factory for {@code resourceDocument} and {@code identifierDocument}. */
  static AmbiguousPrimaryDataCase of(Map args) {
    return new AmbiguousPrimaryDataCase(
        args.id as String,
        args.notes as String,
        args.expectedPath as String,
        args.resourceDocument as JsonApiDocument,
        args.identifierDocument as JsonApiDocument,
        (args.context ?: ValidationContext.defaults()) as ValidationContext)
  }

  JsonApiDocument expectedFor(PrimaryDataKind kind) {
    return kind == PrimaryDataKind.RESOURCE ? resourceDocument : identifierDocument
  }

  @Override
  String toString() {
    return id
  }
}
