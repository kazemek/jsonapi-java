package io.github.kazemek.jsonapi.testfixtures

import java.nio.file.Files
import spock.lang.Specification

class FixtureDirectorySpec extends Specification {

  def "jsonApiFixtures resolves the configured document corpus"() {
    expect:
    Files.isDirectory(FixtureDirectory.jsonApiFixtures())
    Files.isRegularFile(FixtureDirectory.jsonApiFixtures().resolve("manifest.json"))
  }

  def "schemaFixtures resolves the configured draft-schema directory"() {
    expect:
    Files.isDirectory(FixtureDirectory.schemaFixtures())
    Files.isRegularFile(FixtureDirectory.schemaFixtures().resolve("schema.json"))
  }

  def "missing jsonapi.fixtures.dir fails with a clear error"() {
    given:
    def previous = FixtureDirectory.jsonApiFixtures().toString()
    System.clearProperty("jsonapi.fixtures.dir")

    when:
    FixtureDirectory.jsonApiFixtures()

    then:
    def ex = thrown(IllegalStateException)
    ex.message == "System property jsonapi.fixtures.dir must point at fixtures/jsonapi-1.1"

    cleanup:
    System.setProperty("jsonapi.fixtures.dir", previous)
  }

  def "missing jsonapi.schema.fixtures.dir fails with a clear error"() {
    given:
    def previous = FixtureDirectory.schemaFixtures().toString()
    System.clearProperty("jsonapi.schema.fixtures.dir")

    when:
    FixtureDirectory.schemaFixtures()

    then:
    def ex = thrown(IllegalStateException)
    ex.message == "System property jsonapi.schema.fixtures.dir must point at fixtures/jsonapi-schema/1.1-pr1603"

    cleanup:
    System.setProperty("jsonapi.schema.fixtures.dir", previous)
  }
}
