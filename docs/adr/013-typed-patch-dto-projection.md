# ADR-013: Typed PATCH DTO Projection

**Status:** Accepted  
**Date:** 2026-08-17

## Context

ADR-012 established the low-level presence-aware `PatchCommand` / `PatchChange` contract. That
command preserves omitted, explicit-null, and present-value semantics without constructing a
complete DTO, but integrators that prefer resource-specific update models still need to iterate
changes, dispatch on property names, or build their own reflective mapping layer.

## Decision

Add an opt-in typed projection convenience layer that maps an existing `PatchCommand<R>` into an
application-owned patch DTO while keeping the low-level command contract intact.

The neutral presence contract:

- application patch DTO properties use `PatchPresence<T>` with `Omitted` and `Present` variants;
- `Omitted` means the update did not supply the member;
- `Present(null)` means explicit JSON `null` or null relationship linkage;
- `Present(value)` carries the already-converted supplied value from the command.

Jackson 3 projection:

- takes an existing `PatchCommand` only — no JSON re-read, no aggregate re-validation, no domain
  mutation;
- requires `@JsonApiResource` on the patch DTO with the same JSON:API type as the command mapping
  type;
- rejects `@JsonApiId` on patch DTO types; identity remains on `PatchCommand.identity()`;
- allows patch DTOs that expose only a mutable subset of the resource;
- rejects supplied changes that are not representable by the selected patch DTO surface rather than
  silently ignoring them;
- constructs the patch DTO through direct record construction from `PatchPresence` property
  values (patch DTO types must be records; Jackson-major-neutral `PatchPresence` stays
  Jackson-import-free);

Applications own authorization, business validation, and command application. Spring controller
integration and Jackson 2 parity follow as separate adapter work once this contract is stable.

## Consequences

- Applications can choose the raw command path or a typed patch DTO without losing PATCH presence
  semantics.
- Subset patch DTOs remain safe: unmapped supplied changes fail explicitly.
- The low-level command contract stays the lossless boundary; projection is a convenience layer.
- Patch DTO authors must wrap every patchable property in `PatchPresence<T>`.
- Generating patch DTO classes and applying patches to persistence entities remain out of scope.
