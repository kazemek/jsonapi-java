package io.github.kazemek.jsonapi.testfixtures;

/**
 * A shared catalog entry with a stable {@link #id()}. Types that have no notes component inherit
 * {@link #notes()} as the id.
 */
public interface Scenario {

  String id();

  default String notes() {
    return id();
  }
}
