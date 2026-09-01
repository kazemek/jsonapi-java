package io.github.kazemek.jsonapi.testsupport.decoration;

import io.github.kazemek.jsonapi.core.model.ResourceObject;
import java.util.Objects;

/**
 * Adapter-neutral verifier for {@link DecorationScenario} outcomes. Jackson-major suites invoke
 * their own mapper, then hand the result here so Jackson 2 does not copy resource comparison.
 */
public final class DecorationVerifier {

  private DecorationVerifier() {}

  public static void verify(DecorationScenario scenario, ResourceObject actual) {
    Objects.requireNonNull(scenario, "scenario");
    Objects.requireNonNull(actual, "actual");
    ResourceObject expected = scenario.expected();
    if (!expected.type().equals(actual.type())
        || !Objects.equals(expected.id(), actual.id())
        || !Objects.equals(expected.lid(), actual.lid())
        || !Objects.equals(expected.attributes(), actual.attributes())
        || !Objects.equals(expected.relationships(), actual.relationships())
        || !Objects.equals(expected.links(), actual.links())
        || !Objects.equals(expected.meta(), actual.meta())
        || !Objects.equals(expected.additionalMembers(), actual.additionalMembers())) {
      throw new AssertionError(
          "Decoration scenario '"
              + scenario.id()
              + "' mismatch: expected links "
              + expected.links()
              + " relationships "
              + expected.relationships()
              + " but was links "
              + actual.links()
              + " relationships "
              + actual.relationships());
    }
    // Ensure linkage and meta are preserved exactly; relationships comparison above already covers
    // data, links, meta, and additionalMembers per ResourceObject/Relationship equals.
  }
}
