package io.github.kazemek.jsonapi.testfixtures;

import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataScenarios;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios;
import io.github.kazemek.jsonapi.testfixtures.codec.NegativeCodecScenario;
import io.github.kazemek.jsonapi.testfixtures.codec.NegativeCodecScenarios;
import io.github.kazemek.jsonapi.testfixtures.compoundwrite.CompoundWriteScenario;
import io.github.kazemek.jsonapi.testfixtures.compoundwrite.CompoundWriteScenarios;
import io.github.kazemek.jsonapi.testfixtures.domainread.DomainReadScenario;
import io.github.kazemek.jsonapi.testfixtures.domainread.DomainReadScenarios;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenario;
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenarios;
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadScenario;
import io.github.kazemek.jsonapi.testfixtures.enveloperead.EnvelopeReadScenarios;
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetScenario;
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetScenarios;

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

  /** Shared flat resource-to-DTO binding catalog. */
  public static FixtureCatalog<DomainReadScenario> domainRead() {
    return DomainReadScenarios.catalog();
  }

  /** Shared compound-inclusion write catalog. */
  public static FixtureCatalog<CompoundWriteScenario> compoundWrite() {
    return CompoundWriteScenarios.catalog();
  }

  /** Shared sparse-fieldset write catalog. */
  public static FixtureCatalog<SparseFieldsetScenario> sparseFieldset() {
    return SparseFieldsetScenarios.catalog();
  }

  /** Shared typed-envelope read catalog. */
  public static FixtureCatalog<EnvelopeReadScenario> envelopeRead() {
    return EnvelopeReadScenarios.catalog();
  }
}
