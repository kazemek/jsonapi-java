package io.github.kazemek.jsonapi.testsupport;

import java.util.List;
import java.util.function.Predicate;

/**
 * Immutable retrieval surface for a catalog of {@link Scenario} entries. Unknown ids fail with
 * {@link IllegalArgumentException}; the diagnostic label is supplied by the catalog that constructs
 * the instance, not by this contract.
 */
public interface FixtureCatalog<T extends Scenario> {

  /** Immutable catalog entries in registration order. */
  List<T> all();

  /**
   * Looks up an entry by stable id.
   *
   * @throws IllegalArgumentException if {@code id} is not in this catalog
   */
  T byId(String id);

  /** Immutable subset of {@link #all()} matching {@code predicate}. */
  List<T> where(Predicate<? super T> predicate);

  /**
   * Creates an immutable catalog. {@code areaLabel} is the kebab-case catalog name without the
   * {@code Scenarios} suffix and is used only in unknown-id diagnostics ({@code Unknown <areaLabel>
   * scenario id: <id>}).
   */
  static <T extends Scenario> FixtureCatalog<T> of(String areaLabel, List<T> entries) {
    return new ImmutableFixtureCatalog<>(areaLabel, entries);
  }
}
