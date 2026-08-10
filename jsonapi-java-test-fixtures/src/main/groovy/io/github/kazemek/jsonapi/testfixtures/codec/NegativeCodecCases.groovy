package io.github.kazemek.jsonapi.testfixtures.codec

import java.nio.file.Path

import groovy.json.JsonSlurper

/**
 * Manifest-backed read-only negative codec corpus: loads {@code negative-manifest.json} from the
 * {@code jsonapi.fixtures.dir} test system property. The manifest is the source of truth for the
 * closed case list, expected inputs, and version-neutral diagnostics; adapter tests map the
 * category/rule-code strings onto their own enums.
 */
final class NegativeCodecCases {

  private static final List<NegativeCodecCase> ALL = load()

  private NegativeCodecCases() {}

  static List<NegativeCodecCase> all() {
    return ALL
  }

  static NegativeCodecCase byId(String id) {
    def fixture = ALL.find { it.id == id }
    if (fixture == null) {
      throw new IllegalArgumentException("Unknown negative codec case id: " + id)
    }
    return fixture
  }

  private static List<NegativeCodecCase> load() {
    String dir = System.getProperty('jsonapi.fixtures.dir')
    if (dir == null) {
      throw new IllegalStateException(
      'System property jsonapi.fixtures.dir must point at fixtures/jsonapi-1.1')
    }
    def manifest = new JsonSlurper().parse(Path.of(dir).resolve('negative-manifest.json').toFile()) as Map
    return List.copyOf((manifest.cases as List).collect { NegativeCodecCase.of(it) })
  }
}
