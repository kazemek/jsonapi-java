package io.github.kazemek.jsonapi.testsupport.codec;

import io.github.kazemek.jsonapi.testsupport.FixtureCatalog;
import io.github.kazemek.jsonapi.testsupport.codec.cases.AmbiguousEmptyArrayPrimaryDataScenario;
import io.github.kazemek.jsonapi.testsupport.codec.cases.AmbiguousObjectPrimaryDataScenario;
import java.util.List;

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



}
