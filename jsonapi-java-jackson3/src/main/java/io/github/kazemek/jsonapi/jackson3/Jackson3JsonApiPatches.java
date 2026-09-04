package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.core.model.JsonApiDocument;
import io.github.kazemek.jsonapi.jackson.api.JsonApiPatches;
import io.github.kazemek.jsonapi.jackson.patch.PatchCommand;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.Objects;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 Level-1 presence-aware PATCH operations.
 *
 * <p>Typed {@code PatchPresence} DTO binding is the conventional path; {@link PatchCommand} remains
 * the explicit generic/infrastructure projection of the same validated update document. Neither
 * path depends on global resource-type registration and neither reads {@code included}.
 */
final class Jackson3JsonApiPatches implements JsonApiPatches {

  private final JsonMapper baseMapper;
  private final JsonApiPatchReader patchReader;
  private final JsonApiPatchDtoReader patchDtoReader;

  Jackson3JsonApiPatches(
      JsonMapper baseMapper, JsonApiPatchReader patchReader, JsonApiPatchDtoReader patchDtoReader) {
    this.baseMapper = Objects.requireNonNull(baseMapper, "baseMapper");
    this.patchReader = Objects.requireNonNull(patchReader, "patchReader");
    this.patchDtoReader = Objects.requireNonNull(patchDtoReader, "patchDtoReader");
  }

  @Override
  public <T> T readPatch(String json, Class<T> dtoType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(dtoType, "dtoType");
    return patchDtoReader.readValue(json, dtoType);
  }

  @Override
  public <T> T readPatch(InputStream json, Class<T> dtoType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(dtoType, "dtoType");
    return patchDtoReader.readValue(json, dtoType);
  }

  @Override
  public Object readPatch(String json, Type dtoType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(dtoType, "dtoType");
    return patchDtoReader.readValue(json, baseMapper.constructType(dtoType));
  }

  @Override
  public Object readPatch(InputStream json, Type dtoType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(dtoType, "dtoType");
    return patchDtoReader.readValue(json, baseMapper.constructType(dtoType));
  }

  @Override
  public <T> PatchCommand<T> readCommand(String json, Class<T> resourceType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(resourceType, "resourceType");
    return patchReader.readValue(json, resourceType);
  }

  @Override
  public <T> PatchCommand<T> readCommand(InputStream json, Class<T> resourceType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(resourceType, "resourceType");
    return patchReader.readValue(json, resourceType);
  }

  @Override
  @SuppressWarnings("java:S1452")
  public PatchCommand<?> readCommand(String json, Type resourceType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(resourceType, "resourceType");
    return patchReader.readValue(json, baseMapper.constructType(resourceType));
  }

  @Override
  @SuppressWarnings("java:S1452")
  public PatchCommand<?> readCommand(InputStream json, Type resourceType) {
    Objects.requireNonNull(json, "json");
    Objects.requireNonNull(resourceType, "resourceType");
    return patchReader.readValue(json, baseMapper.constructType(resourceType));
  }

  @Override
  public <T> T bindPatch(JsonApiDocument document, Class<T> dtoType) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(dtoType, "dtoType");
    return patchDtoReader.fromDocument(document, dtoType);
  }

  @Override
  public Object bindPatch(JsonApiDocument document, Type dtoType) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(dtoType, "dtoType");
    return patchDtoReader.fromDocument(document, baseMapper.constructType(dtoType));
  }

  @Override
  public <T> PatchCommand<T> bindCommand(JsonApiDocument document, Class<T> resourceType) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(resourceType, "resourceType");
    return patchReader.fromDocument(document, resourceType);
  }

  @Override
  @SuppressWarnings("java:S1452")
  public PatchCommand<?> bindCommand(JsonApiDocument document, Type resourceType) {
    Objects.requireNonNull(document, "document");
    Objects.requireNonNull(resourceType, "resourceType");
    return patchReader.fromDocument(document, baseMapper.constructType(resourceType));
  }
}
