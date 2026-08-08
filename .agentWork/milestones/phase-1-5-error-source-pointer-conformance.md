# Phase 1.5 — Error Source Pointer Conformance

> **Module:** `jsonapi-java-core`
> **Packages:** `io.github.kazemek.jsonapi.core.model`, `io.github.kazemek.jsonapi.core.internal`,
> `io.github.kazemek.jsonapi.core.validation`
> **Dependencies:** Phase 1.1, Phase 1.4
> **Status:** Complete

## Goal

Make `ErrorSource.pointer` reject non–RFC 6901 JSON Pointer syntax while keeping the existing
public API and architecture intact.

## Research and constraints

- [JSON:API 1.1 error objects](https://jsonapi.org/format/1.1/#error-objects) — `errors[].source.pointer`
  is a JSON Pointer identifying a value in the request document. Consequence: validate pointer
  syntax; do not resolve against a document in core.
- [RFC 6901](https://www.rfc-editor.org/rfc/rfc6901) — empty string `""` is a valid pointer; a
  non-empty pointer must start with `/`; only `~0` and `~1` are valid escapes inside a reference
  token (`~01` is valid; bare `~`, `~2`, and other `~` forms are invalid); reference tokens may
  contain Unicode. Do not accept URI-fragment form (`#/…`) for `ErrorSource.pointer`.
- [ADR-003](../../docs/adr/003-validation-and-immutability.md) — local invariants belong in the
  public construction path via stable `ValidationRuleCode` and JSON Pointer-like paths; keep
  local vs aggregate separation; do not extract a new validation service for this change.
- Existing pattern: [`Link`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/model/Link.java)
  uses [`SyntaxValidators`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/internal/SyntaxValidators.java)
  + `LocalValidation.fail` at compact-constructor time. [`ErrorSource`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/model/ErrorSource.java)
  currently validates only `additionalMembers` under base path `/errors/source` and accepts arbitrary
  `pointer` strings — this is a stricter-validation behavior change for previously accepted invalid
  pointers.
- [`docs/conformance.md`](../../docs/conformance.md) has “Error source additional members” but no
  `source.pointer` / RFC 6901 row yet; add one (syntax-only, no resolution).
- Existing [`JsonPointers`](../../jsonapi-java-core/src/main/java/io/github/kazemek/jsonapi/core/internal/JsonPointers.java)
  is emit/escape/build only — do **not** extend it for inbound syntax validation; put the check in
  `SyntaxValidators` like other inbound syntax helpers.
- No new ADR. Nullness unchanged (`pointer` already `@Nullable`). Public validate-flow / diagnostic
  surface changes (`ErrorSource` construction + new `ValidationRuleCode`), so refresh
  `jsonapi-java-core` docs per the `module-docs` skill (do not copy its checklist into this file).

## Deliverables

1. **JSON Pointer syntax helper** in `SyntaxValidators` (e.g. `isValidJsonPointer(String)`),
   syntax-only, matching existing internal conventions; no new package or public type; do not
   extend `JsonPointers` for validation.
2. **`ErrorSource` constructor validation:** when `pointer` is non-null, accept valid RFC 6901
   syntax and reject invalid syntax with `ValidationRuleCode.INVALID_JSON_POINTER` at
   `/errors/source/pointer`; `null` remains valid (optional member).
3. **Focused Spock coverage:** extend `SyntaxValidatorsSpec` for the RFC 6901 boolean matrix, and
   model/`ErrorSource` specs for `INVALID_JSON_POINTER` + `/errors/source/pointer` (including `""`,
   escapes, `~01`, bare `~`, missing leading `/`, Unicode, and rejected URI-fragment `#/…`).
4. **Conformance row** in `docs/conformance.md` marking `source.pointer` RFC 6901 syntax as
   supported, syntax-only, with no document resolution in core.
5. **Module docs** for `jsonapi-java-core` per the `module-docs` skill: agent/invariant and
   entry-point notes for syntax-only `ErrorSource.pointer` RFC 6901 validation, linking
   conformance.

## Non-goals

- Resolving pointers against request/response documents or checking that a pointer targets a
  valid JSON:API member.
- URI-fragment JSON Pointer syntax (`#/…`).
- Semantic validation of `parameter`, `header`, or broader `ErrorObject` rules.
- Public `JsonPointer` type, new packages, new validation services, extracting `SyntaxValidators`,
  extending `JsonPointers` for inbound validation, new dependencies, or a new ADR.
- Phase 4.1 conformance/hardening umbrella work; Jackson/Spring modules.

## Implementation boundaries

- Touch `jsonapi-java-core` only: `SyntaxValidators`, `ErrorSource`, `ValidationRuleCode`, mirrored
  Spock specs (`SyntaxValidatorsSpec` and model/`ErrorSource`), `docs/conformance.md`, and
  `jsonapi-java-core` module docs required by `module-docs`.
- Do not change `ErrorSource`’s public record shape, `ErrorObject`, or `JsonApiDocumentValidator`
  unless the existing local-validation architecture absolutely requires it (prefer constructor-only).
- Public API surface unchanged except stricter rejection of invalid pointer strings and one new
  public enum constant on `ValidationRuleCode`.

## Test strategy

- Helper (`SyntaxValidatorsSpec`): boolean table for `""`, `/`, `/data`, `/data/0/id`, `/a~0b`,
  `/a~1b`, `/a~01b`, Unicode token path (e.g. `/données`) → true; `data`, `/a~`, `/a~2`, `/a~x`,
  `#/data` → false.
- Model/`ErrorSource`: `pointer == null` and valid pointers construct successfully; invalid
  pointers throw `JsonApiValidationException` with `ruleCode == INVALID_JSON_POINTER` and path
  `/errors/source/pointer` (mirror `LinkSpec` conventions).

## Acceptance criteria

- [x] Non-null `ErrorSource.pointer` accepts RFC 6901 syntax (including `""`, `~0`/`~1`/`~01`, and
      Unicode tokens) and rejects invalid syntax with `INVALID_JSON_POINTER` at
      `/errors/source/pointer`.
- [x] `pointer == null` remains valid; no public API redesign, no document-resolution logic, and no
      new dependency.
- [x] `docs/conformance.md` documents syntax-only RFC 6901 support for `source.pointer`.
- [x] The canonical `module-docs` checklist passes for `jsonapi-java-core`.
- [x] `./gradlew :jsonapi-java-core:test --tests 'io.github.kazemek.jsonapi.core.internal.SyntaxValidatorsSpec' --tests 'io.github.kazemek.jsonapi.core.model.*'`
      passes.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [x] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, report Sonar
      blocked and that CI must still pass the gate.
