package io.github.kazemek.jsonapi.testfixtures.codec

import java.util.Objects

/**
 * One read-only negative codec case: a wire input that must fail to decode, with the
 * version-neutral expected failure category, JSON Pointer, and core rule code (each recorded
 * only when present). Category and rule-code values are manifest strings so no Jackson-major
 * adapter types leak into the shared corpus.
 */
final class NegativeCodecCase {
  final String id
  final String notes
  final String path
  final String category
  final String pointer
  final String ruleCode
  final boolean sourceLocation

  private NegativeCodecCase(
  String id,
  String notes,
  String path,
  String category,
  String pointer,
  String ruleCode,
  boolean sourceLocation) {
    this.id = Objects.requireNonNull(id, "id")
    this.notes = Objects.requireNonNull(notes, "notes")
    this.path = Objects.requireNonNull(path, "path")
    this.category = Objects.requireNonNull(category, "category")
    this.pointer = pointer
    this.ruleCode = ruleCode
    this.sourceLocation = sourceLocation
  }

  static NegativeCodecCase of(Map args) {
    return new NegativeCodecCase(
        args.id as String,
        args.notes as String,
        args.path as String,
        args.category as String,
        args.pointer as String,
        args.ruleCode as String,
        (args.sourceLocation ?: false) as boolean)
  }

  @Override
  String toString() {
    return id
  }
}
