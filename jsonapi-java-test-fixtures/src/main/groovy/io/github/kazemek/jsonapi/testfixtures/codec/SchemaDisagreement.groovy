package io.github.kazemek.jsonapi.testfixtures.codec

/**
 * Documented disagreement between a fixture and the pinned JSON:API 1.1 draft-PR schemas. The
 * fixture must keep failing the draft for the recorded reason, so a schema fix forces an
 * intentional re-review; a schema result never changes a conformance status.
 */
final class SchemaDisagreement {
  final String reason
  final List<Map<String, String>> expected

  private SchemaDisagreement(String reason, List<Map<String, String>> expected) {
    this.reason = Objects.requireNonNull(reason, "reason")
    this.expected = List.copyOf(Objects.requireNonNull(expected, "expected"))
  }

  /**
   * Named-argument form: {@code SchemaDisagreement.of(reason: '...', expected: [[keyword: 'not',
   * path: '/data']])}. Each expected entry records the failing JSON Schema {@code keyword} and the
   * {@code path} of the instance location.
   */
  static SchemaDisagreement of(Map args) {
    return new SchemaDisagreement(
        args.reason as String,
        ((args.expected ?: []) as List).collect { Map.copyOf(it) })
  }
}
