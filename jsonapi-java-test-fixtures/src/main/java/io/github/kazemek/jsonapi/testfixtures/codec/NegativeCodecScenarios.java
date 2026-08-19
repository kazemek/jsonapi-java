package io.github.kazemek.jsonapi.testfixtures.codec;

import io.github.kazemek.jsonapi.testfixtures.FixtureCatalog;
import io.github.kazemek.jsonapi.testfixtures.FixtureDirectory;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.Nullable;

/**
 * Manifest-backed read-only negative codec corpus: loads {@code negative-manifest.json} from the
 * directory resolved by {@link FixtureDirectory}. The manifest is the source of the scenario
 * metadata (inputs and version-neutral diagnostics); {@code NegativeCodecScenariosCatalogSpec}
 * additionally pins the intentionally closed case set. Adapter tests map the category/rule-code
 * strings onto their own enums.
 */
public final class NegativeCodecScenarios {

  private static final FixtureCatalog<NegativeCodecScenario> CATALOG =
      FixtureCatalog.of("negative-codec", load());

  private static final String SOURCE_LOCATION = "sourceLocation";

  private NegativeCodecScenarios() {}

  public static FixtureCatalog<NegativeCodecScenario> catalog() {
    return CATALOG;
  }

  public static List<NegativeCodecScenario> all() {
    return CATALOG.all();
  }

  public static NegativeCodecScenario byId(String id) {
    return CATALOG.byId(id);
  }

  public static List<NegativeCodecScenario> where(
      Predicate<? super NegativeCodecScenario> predicate) {
    return CATALOG.where(predicate);
  }

  private static List<NegativeCodecScenario> load() {
    Path manifestPath = FixtureDirectory.jsonApiFixtures().resolve("negative-manifest.json");
    try (JsonReader reader = Json.createReader(Files.newBufferedReader(manifestPath))) {
      JsonObject manifest = reader.readObject();
      JsonArray cases = manifest.getJsonArray("cases");
      List<NegativeCodecScenario> loaded = new ArrayList<>(cases.size());
      for (int i = 0; i < cases.size(); i++) {
        JsonObject entry = cases.getJsonObject(i);
        loaded.add(
            new NegativeCodecScenario(
                entry.getString("id"),
                entry.getString("notes"),
                entry.getString("path"),
                entry.getString("category"),
                optionalString(entry, "pointer"),
                optionalString(entry, "ruleCode"),
                entry.containsKey(SOURCE_LOCATION)
                    && !entry.isNull(SOURCE_LOCATION)
                    && entry.getBoolean(SOURCE_LOCATION)));
      }
      return List.copyOf(loaded);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static @Nullable String optionalString(JsonObject object, String name) {
    if (!object.containsKey(name) || object.isNull(name)) {
      return null;
    }
    return object.getString(name);
  }
}
