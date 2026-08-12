package io.github.kazemek.jsonapi.testfixtures;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

final class ImmutableFixtureCatalog<T extends Scenario> implements FixtureCatalog<T> {

  private final String areaLabel;
  private final List<T> all;
  private final Map<String, T> index;

  ImmutableFixtureCatalog(String areaLabel, List<T> entries) {
    this.areaLabel = Objects.requireNonNull(areaLabel, "areaLabel");
    this.all = List.copyOf(Objects.requireNonNull(entries, "entries"));
    Map<String, T> byId = new LinkedHashMap<>();
    for (T entry : this.all) {
      byId.put(entry.id(), entry);
    }
    this.index = Map.copyOf(byId);
  }

  @Override
  public List<T> all() {
    return all;
  }

  @Override
  public T byId(String id) {
    T found = index.get(id);
    if (found == null) {
      throw new IllegalArgumentException("Unknown " + areaLabel + " scenario id: " + id);
    }
    return found;
  }

  @Override
  public List<T> where(Predicate<? super T> predicate) {
    Objects.requireNonNull(predicate, "predicate");
    return all.stream().filter(predicate).toList();
  }
}
