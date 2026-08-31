package io.github.kazemek.jsonapi.testsupport

import spock.lang.Specification

// Why this spec exists: FixtureCatalog is the shared catalog implementation used by every feature
// catalog. Duplicate ids, registration order, byId, unknown-id area labels, where, and
// immutability belong here once. Feature *ScenariosCatalogSpec classes must not re-test this API.
class FixtureCatalogSpec extends Specification {

  def "construction rejects duplicate scenario ids"() {
    given:
    def scenario = entry("dup")

    when:
    FixtureCatalog.of("codec", [scenario, scenario])

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Duplicate codec scenario id: dup"
  }

  def "construction preserves registration order"() {
    given:
    def first = entry("a")
    def second = entry("b")
    def third = entry("c")

    expect:
    FixtureCatalog.of("demo", [first, second, third]).all()*.id() == ["a", "b", "c"]
  }

  def "all is an unmodifiable snapshot of registered entries"() {
    given:
    def catalog = FixtureCatalog.of("demo", [entry("a"), entry("b")])

    when:
    catalog.all().add(entry("c"))

    then:
    thrown(UnsupportedOperationException)
    catalog.all()*.id() == ["a", "b"]
  }

  def "byId returns each registered scenario by identity"() {
    given:
    def first = entry("a")
    def second = entry("b")
    def catalog = FixtureCatalog.of("demo", [first, second])

    expect:
    catalog.byId("a").is(first)
    catalog.byId("b").is(second)
  }

  def "byId rejects unknown ids with the area-label diagnostic"() {
    given:
    def catalog = FixtureCatalog.of("domain-write", [entry("known")])

    when:
    catalog.byId("missing")

    then:
    def ex = thrown(IllegalArgumentException)
    ex.message == "Unknown domain-write scenario id: missing"
  }

  def "where keeps matching entries in registration order"() {
    given:
    def catalog = FixtureCatalog.of("demo", [
      entry("keep-1"),
      entry("skip"),
      entry("keep-2")
    ])

    expect:
    catalog.where { it.id().startsWith("keep") }*.id() == ["keep-1", "keep-2"]
    catalog.where { true }*.id() == catalog.all()*.id()
    catalog.where { false }.isEmpty()
  }

  def "where returns an unmodifiable list that does not alias all"() {
    given:
    def catalog = FixtureCatalog.of("demo", [entry("a"), entry("b")])
    def filtered = catalog.where { true }

    when:
    filtered.add(entry("c"))

    then:
    thrown(UnsupportedOperationException)
    catalog.all()*.id() == ["a", "b"]
    !filtered.is(catalog.all())
  }

  def "where rejects a null predicate"() {
    given:
    def catalog = FixtureCatalog.of("demo", [entry("a")])

    when:
    catalog.where(null)

    then:
    thrown(NullPointerException)
  }

  def "construction rejects a null area label, entry list, or entry id"() {
    when:
    FixtureCatalog.of(null, [entry("a")])

    then:
    thrown(NullPointerException)

    when:
    FixtureCatalog.of("demo", null)

    then:
    thrown(NullPointerException)

    when:
    FixtureCatalog.of("demo", [entry(null)])

    then:
    thrown(NullPointerException)
  }

  private static Scenario entry(String id) {
    return new Scenario() {
          @Override
          String id() {
            return id
          }
        }
  }
}
