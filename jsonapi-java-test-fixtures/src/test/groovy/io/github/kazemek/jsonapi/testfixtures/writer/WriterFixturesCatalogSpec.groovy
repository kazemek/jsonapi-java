package io.github.kazemek.jsonapi.testfixtures.writer

import java.nio.file.Files
import java.nio.file.Path

import groovy.json.JsonSlurper
import spock.lang.Shared
import spock.lang.Specification

class WriterFixturesCatalogSpec extends Specification {

  @Shared
  Path fixturesDir = Path.of(System.getProperty("jsonapi.fixtures.dir"))

  def "catalog ids match manifest and every expected path exists"() {
    given:
    def manifest = new JsonSlurper().parse(fixturesDir.resolve("manifest.json").toFile()) as Map
    def manifestIds = (manifest.fixtures as List).collect { it.id as String }
    def catalogIds = WriterFixtures.all()*.id

    expect:
    catalogIds == manifestIds

    and:
    WriterFixtures.all().each { fixture ->
      assert Files.isRegularFile(fixturesDir.resolve(fixture.expectedPath))
      if (fixture.assertExactUtf8) {
        assert fixture.exactUtf8Path != null
        assert Files.isRegularFile(fixturesDir.resolve(fixture.exactUtf8Path))
      }
    }
  }

  def "byId returns each registered fixture"() {
    expect:
    WriterFixtures.all().every { WriterFixtures.byId(it.id).is(it) }
  }
}
