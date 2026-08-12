package io.github.kazemek.jsonapi.testfixtures;

import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataScenarios;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios;
import io.github.kazemek.jsonapi.testfixtures.codec.NegativeCodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.NegativeCodecScenarios;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenario;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenarios;

/**
 * Canonical retrieval facade for the shared fixture catalogs. Adapter suites and future catalogs
 * use this type together with {@link FixtureCatalog}; the concrete {@code *Scenarios} classes
 * retain static shims for existing consumers. Capability selection is {@link FixtureCatalog#where}.
 */
public final class JsonApiFixtures {

  private JsonApiFixtures() {}

  public static FixtureCatalog<CodecScenario> codec() {
    return CodecScenarios.catalog();
  }

  public static FixtureCatalog<NegativeCodecScenario> negativeCodec() {
    return NegativeCodecScenarios.catalog();
  }

  public static FixtureCatalog<AmbiguousPrimaryDataScenario> ambiguousPrimaryData() {
    return AmbiguousPrimaryDataScenarios.catalog();
  }

  public static FixtureCatalog<DomainWriteScenario> domainWrite() {
    return DomainWriteScenarios.catalog();
  }
}
