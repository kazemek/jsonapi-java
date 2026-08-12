package io.github.kazemek.jsonapi.testfixtures.codec;

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
import org.jspecify.annotations.Nullable;

/**
 * Manifest-backed read-only negative codec corpus: loads {@code negative-manifest.json} from the
 * {@code jsonapi.fixtures.dir} test system property. The manifest is the source of the case
 * metadata (inputs and version-neutral diagnostics); {@code NegativeCodecCasesCatalogSpec}
 * additionally pins the intentionally closed case set. Adapter tests map the category/rule-code
 * strings onto their own enums.
 */
public final class NegativeCodecCases {

  private static final List<NegativeCodecCase> ALL = load();

  private NegativeCodecCases() {}

  public static List<NegativeCodecCase> all() {
    return ALL;
  }

  public static NegativeCodecCase byId(String id) {
    for (NegativeCodecCase fixture : ALL) {
      if (fixture.id().equals(id)) {
        return fixture;
      }
    }
    throw new IllegalArgumentException("Unknown negative codec case id: " + id);
  }

  private static List<NegativeCodecCase> load() {
    String dir = System.getProperty("jsonapi.fixtures.dir");
    if (dir == null) {
      throw new IllegalStateException(
          "System property jsonapi.fixtures.dir must point at fixtures/jsonapi-1.1");
    }
    Path manifestPath = Path.of(dir).resolve("negative-manifest.json");
    try (JsonReader reader = Json.createReader(Files.newBufferedReader(manifestPath))) {
      JsonObject manifest = reader.readObject();
      JsonArray cases = manifest.getJsonArray("cases");
      List<NegativeCodecCase> loaded = new ArrayList<>(cases.size());
      for (int i = 0; i < cases.size(); i++) {
        JsonObject entry = cases.getJsonObject(i);
        loaded.add(
            new NegativeCodecCase(
                entry.getString("id"),
                entry.getString("notes"),
                entry.getString("path"),
                entry.getString("category"),
                optionalString(entry, "pointer"),
                optionalString(entry, "ruleCode"),
                booleanOrFalse(entry, "sourceLocation")));
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

  private static boolean booleanOrFalse(JsonObject object, String name) {
    if (!object.containsKey(name) || object.isNull(name)) {
      return false;
    }
    return object.getBoolean(name);
  }
}
