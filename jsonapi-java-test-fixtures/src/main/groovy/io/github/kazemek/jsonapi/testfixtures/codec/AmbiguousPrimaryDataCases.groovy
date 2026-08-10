package io.github.kazemek.jsonapi.testfixtures.codec

import io.github.kazemek.jsonapi.testfixtures.codec.cases.AmbiguousEmptyArrayPrimaryDataCase
import io.github.kazemek.jsonapi.testfixtures.codec.cases.AmbiguousObjectPrimaryDataCase

/**
 * Explicit catalog of shared dual-success ambiguous primary-data cases in manifest order.
 */
final class AmbiguousPrimaryDataCases {

  private static final List<AmbiguousPrimaryDataCase> ALL = List.copyOf([
    AmbiguousObjectPrimaryDataCase.fixture(),
    AmbiguousEmptyArrayPrimaryDataCase.fixture(),
  ])

  private static final Map<String, AmbiguousPrimaryDataCase> BY_ID =
  ALL.collectEntries { [(it.id): it] } as Map<String, AmbiguousPrimaryDataCase>

  private AmbiguousPrimaryDataCases() {}

  static List<AmbiguousPrimaryDataCase> all() {
    return ALL
  }

  static AmbiguousPrimaryDataCase byId(String id) {
    def fixture = BY_ID[id]
    if (fixture == null) {
      throw new IllegalArgumentException("Unknown ambiguous primary-data case id: " + id)
    }
    return fixture
  }
}
