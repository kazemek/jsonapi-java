package io.github.kazemek.jsonapi.testfixtures;

import java.nio.file.Path;

/**
 * Centralized resolution of the {@code jsonapi.fixtures.dir} and {@code
 * jsonapi.schema.fixtures.dir} test system properties wired by the {@code jsonapi-java-library}
 * convention plugin.
 */
public final class FixtureDirectory {

  private FixtureDirectory() {}

  /**
   * Directory configured by {@code jsonapi.fixtures.dir} (the JSON:API 1.1 document corpus).
   *
   * @throws IllegalStateException if the property is missing
   */
  public static Path jsonApiFixtures() {
    return requiredDirectory("jsonapi.fixtures.dir", "fixtures/jsonapi-1.1");
  }

  /**
   * Directory configured by {@code jsonapi.schema.fixtures.dir} (the pinned draft-PR schemas).
   *
   * @throws IllegalStateException if the property is missing
   */
  public static Path schemaFixtures() {
    return requiredDirectory("jsonapi.schema.fixtures.dir", "fixtures/jsonapi-schema/1.1-pr1603");
  }

  private static Path requiredDirectory(String property, String expectedLocation) {
    String dir = System.getProperty(property);
    if (dir == null) {
      throw new IllegalStateException(
          "System property " + property + " must point at " + expectedLocation);
    }
    return Path.of(dir);
  }
}
