package io.github.kazemek.jsonapi.testfixtures

import io.github.kazemek.jsonapi.testfixtures.codec.AmbiguousPrimaryDataScenarios
import io.github.kazemek.jsonapi.testfixtures.codec.CodecScenarios
import io.github.kazemek.jsonapi.testfixtures.codec.NegativeCodecScenarios
import io.github.kazemek.jsonapi.testfixtures.domainwrite.DomainWriteScenarios
import spock.lang.Specification

class JsonApiFixturesSpec extends Specification {

  def "typed accessors return the catalog views"() {
    expect:
    JsonApiFixtures.codec().all() == CodecScenarios.all()
    JsonApiFixtures.negativeCodec().all() == NegativeCodecScenarios.all()
    JsonApiFixtures.ambiguousPrimaryData().all() == AmbiguousPrimaryDataScenarios.all()
    JsonApiFixtures.domainWrite().all() == DomainWriteScenarios.all()
  }

  def "each accessor returns the same instance as the catalog catalog() accessor"() {
    expect:
    JsonApiFixtures.codec().is(CodecScenarios.catalog())
    JsonApiFixtures.negativeCodec().is(NegativeCodecScenarios.catalog())
    JsonApiFixtures.ambiguousPrimaryData().is(AmbiguousPrimaryDataScenarios.catalog())
    JsonApiFixtures.domainWrite().is(DomainWriteScenarios.catalog())
  }

  def "every catalog where shim returns the full catalog for a matching predicate"() {
    expect:
    CodecScenarios.where({ true })*.id == CodecScenarios.all()*.id
    NegativeCodecScenarios.where({ true })*.id == NegativeCodecScenarios.all()*.id
    AmbiguousPrimaryDataScenarios.where({ true })*.id == AmbiguousPrimaryDataScenarios.all()*.id
    DomainWriteScenarios.where({ true })*.id == DomainWriteScenarios.all()*.id
  }

  def "capability filtering works through where"() {
    expect:
    JsonApiFixtures.codec().where { it.writable }*.id == CodecScenarios.writable()*.id
  }

  def "every domain-write entry satisfies the Scenario notes default contract"() {
    expect:
    DomainWriteScenarios.all().every { it.notes() == it.id() }
  }

  def "domain-write unknown byId fails with the unified message"() {
    when:
    DomainWriteScenarios.byId("no such scenario")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown domain-write scenario id: no such scenario"
  }
}
