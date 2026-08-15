package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import java.util.List;
import java.util.function.Predicate;

/** Explicit catalog of shared dual-success ambiguous primary-data scenarios in manifest order. */
public final class AmbiguousPrimaryDataScenarios {

  private static final FixtureCatalog<AmbiguousPrimaryDataScenario> CATALOG =
      FixtureCatalog.of(
          "ambiguous-primary-data",
          List.of(
              AmbiguousObjectPrimaryDataScenario.scenario(),
              AmbiguousEmptyArrayPrimaryDataScenario.scenario()));

  private AmbiguousPrimaryDataScenarios() {}

  public static FixtureCatalog<AmbiguousPrimaryDataScenario> catalog() {
    return CATALOG;
  }

  public static List<AmbiguousPrimaryDataScenario> all() {
    return CATALOG.all();
  }

  public static AmbiguousPrimaryDataScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<AmbiguousPrimaryDataScenario> where(
      Predicate<? super AmbiguousPrimaryDataScenario> predicate) {
    return CATALOG.where(predicate);
  }
}
