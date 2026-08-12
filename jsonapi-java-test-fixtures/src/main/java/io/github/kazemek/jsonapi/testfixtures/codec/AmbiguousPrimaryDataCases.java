package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.testfixtures.codec.cases.AmbiguousEmptyArrayPrimaryDataCase;
import io.github.kazemek.jsonapi.testfixtures.codec.cases.AmbiguousObjectPrimaryDataCase;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Explicit catalog of shared dual-success ambiguous primary-data cases in manifest order. */
public final class AmbiguousPrimaryDataCases {

  private static final List<AmbiguousPrimaryDataCase> ALL =
      List.of(
          AmbiguousObjectPrimaryDataCase.fixture(), AmbiguousEmptyArrayPrimaryDataCase.fixture());

  private static final Map<String, AmbiguousPrimaryDataCase> BY_ID = indexById(ALL);

  private AmbiguousPrimaryDataCases() {}

  public static List<AmbiguousPrimaryDataCase> all() {
    return ALL;
  }

  public static AmbiguousPrimaryDataCase byId(String id) {
    AmbiguousPrimaryDataCase fixture = BY_ID.get(id);
    if (fixture == null) {
      throw new IllegalArgumentException("Unknown ambiguous primary-data case id: " + id);
    }
    return fixture;
  }

  private static Map<String, AmbiguousPrimaryDataCase> indexById(
      List<AmbiguousPrimaryDataCase> cases) {
    Map<String, AmbiguousPrimaryDataCase> index = new LinkedHashMap<>();
    for (AmbiguousPrimaryDataCase fixture : cases) {
      index.put(fixture.id(), fixture);
    }
    return Map.copyOf(index);
  }
}
