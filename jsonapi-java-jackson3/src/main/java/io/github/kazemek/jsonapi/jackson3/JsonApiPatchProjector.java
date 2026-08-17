package io.github.kazemek.jsonapi.jackson3;

import io.github.kazemek.jsonapi.jackson.PatchCommand;
import io.github.kazemek.jsonapi.jackson3.internal.DomainPatchProjector;
import java.util.Objects;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Projects an existing {@link PatchCommand} into an application-owned patch DTO.
 *
 * <p>Construct instances via {@link JsonApiJackson3#patchProjector(JsonMapper)} or its overloads,
 * never directly. The projector is safe for concurrent use once created. Projection is read-only:
 * it never re-parses JSON, never re-validates the update document, and never mutates domain state.
 *
 * <p>Each patchable property on the target type must be {@link
 * io.github.kazemek.jsonapi.jackson.PatchPresence}. Resource identity remains on {@link
 * PatchCommand#identity()}; patch DTO types must not declare {@code @JsonApiId}. Supplied changes
 * that are not representable by the selected patch DTO surface fail with {@link
 * MappingDiagnostic#UNREPRESENTABLE_PATCH_CHANGE}.
 */
public final class JsonApiPatchProjector {

  private static final String COMMAND = "command";
  private static final String PATCH_TYPE = "patchType";

  private final JsonMapper mapper;
  private final DomainPatchProjector projector;

  JsonApiPatchProjector(JsonMapper mapper, DomainPatchProjector projector) {
    this.mapper = Objects.requireNonNull(mapper, "mapper");
    this.projector = Objects.requireNonNull(projector, "projector");
  }

  /** Projects one patch command into the given patch DTO type. */
  public <P> P project(PatchCommand<?> command, Class<P> patchType) {
    Objects.requireNonNull(command, COMMAND);
    Objects.requireNonNull(patchType, PATCH_TYPE);
    return patchType.cast(projector.project(command, mapper.constructType(patchType)));
  }

  /** Projects one patch command into the given patch DTO Java type. */
  public Object project(PatchCommand<?> command, JavaType patchType) {
    Objects.requireNonNull(command, COMMAND);
    Objects.requireNonNull(patchType, PATCH_TYPE);
    return projector.project(command, patchType);
  }
}
