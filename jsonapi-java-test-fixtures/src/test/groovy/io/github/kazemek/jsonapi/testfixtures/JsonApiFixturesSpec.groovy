package io.github.kazemek.jsonapi.testfixtures

import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataScenarios
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios
import io.github.kazemek.jsonapi.testfixtures.codec.NegativeCodecScenarios
import io.github.kazemek.jsonapi.testfixtures.compoundwrite.CompoundWriteScenarios
import io.github.kazemek.jsonapi.testfixtures.domainread.DomainReadScenarios
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenarios
import io.github.kazemek.jsonapi.testfixtures.sparsefieldset.SparseFieldsetScenarios
import spock.lang.Specification

class JsonApiFixturesSpec extends Specification {

  def "typed accessors return the catalog views"() {
    expect:
    JsonApiFixtures.codec().all() == CodecScenarios.all()
    JsonApiFixtures.negativeCodec().all() == NegativeCodecScenarios.all()
    JsonApiFixtures.ambiguousPrimaryData().all() == AmbiguousPrimaryDataScenarios.all()
    JsonApiFixtures.domainWrite().all() == DomainWriteScenarios.all()
    JsonApiFixtures.domainRead().all() == DomainReadScenarios.all()
    JsonApiFixtures.compoundWrite().all() == CompoundWriteScenarios.all()
    JsonApiFixtures.sparseFieldset().all() == SparseFieldsetScenarios.all()
  }

  def "each accessor returns the same instance as the catalog catalog() accessor"() {
    expect:
    JsonApiFixtures.codec().is(CodecScenarios.catalog())
    JsonApiFixtures.negativeCodec().is(NegativeCodecScenarios.catalog())
    JsonApiFixtures.ambiguousPrimaryData().is(AmbiguousPrimaryDataScenarios.catalog())
    JsonApiFixtures.domainWrite().is(DomainWriteScenarios.catalog())
    JsonApiFixtures.domainRead().is(DomainReadScenarios.catalog())
    JsonApiFixtures.compoundWrite().is(CompoundWriteScenarios.catalog())
    JsonApiFixtures.sparseFieldset().is(SparseFieldsetScenarios.catalog())
  }

  def "every catalog where shim returns the full catalog for a matching predicate"() {
    expect:
    CodecScenarios.where({ true })*.id == CodecScenarios.all()*.id
    NegativeCodecScenarios.where({ true })*.id == NegativeCodecScenarios.all()*.id
    AmbiguousPrimaryDataScenarios.where({ true })*.id == AmbiguousPrimaryDataScenarios.all()*.id
    DomainWriteScenarios.where({ true })*.id == DomainWriteScenarios.all()*.id
    DomainReadScenarios.where({ true })*.id == DomainReadScenarios.all()*.id
    CompoundWriteScenarios.where({ true })*.id == CompoundWriteScenarios.all()*.id
    SparseFieldsetScenarios.where({ true })*.id == SparseFieldsetScenarios.all()*.id
  }

  def "capability filtering works through where"() {
    expect:
    JsonApiFixtures.codec().where { it.writable }*.id == CodecScenarios.writable()*.id
  }

  def "every domain-write entry satisfies the Scenario notes default contract"() {
    expect:
    DomainWriteScenarios.all().every { it.notes() == it.id() }
  }

  def "every domain-read entry satisfies the Scenario notes default contract"() {
    expect:
    DomainReadScenarios.all().every { it.notes() == it.id() }
  }

  def "every compound-write entry satisfies the Scenario notes default contract"() {
    expect:
    CompoundWriteScenarios.all().every { it.notes() == it.id() }
  }

  def "every sparse-fieldset entry satisfies the Scenario notes default contract"() {
    expect:
    SparseFieldsetScenarios.all().every { it.notes() == it.id() }
  }

  def "domain-write unknown byId fails with the unified message"() {
    when:
    DomainWriteScenarios.byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown domain-write scenario id: no such scenario"
  }

  def "domain-read unknown byId fails with the unified message"() {
    when:
    DomainReadScenarios.byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown domain-read scenario id: no such scenario"
  }

  def "compound-write unknown byId fails with the unified message"() {
    when:
    CompoundWriteScenarios.byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown compound-write scenario id: no such scenario"
  }

  def "sparse-fieldset unknown byId fails with the unified message"() {
    when:
    SparseFieldsetScenarios.byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown sparse-fieldset scenario id: no such scenario"
  }
}
