# Phase 2.14 — Shared Domain Read Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `ResourceBinderSpec`  
> **Dependencies:** Phases 2.9 and 2.11  
> **Status:** Not started

## Goal

Provide one version-neutral flat resource-to-DTO binding scenario catalog that proves binder parity
while preserving graph-free linkage-only semantics.

## Research and constraints

- Phase 2.9 defines validated resource-to-DTO binding independently of JSON parsing and included
  resources; shared cases must not collapse binder and document-reader responsibilities.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) requires linkage-only relationship binding
  and never reads `included` for DTO relationships.
- Closed shared `ResourceBinderSpec` test names:
  `binds record with id, attributes, and built-in ResourceIdentifier relationships`;
  `binds mutable POJO`; `binds immutable creator-based POJO`; `binds inherited properties`;
  `binds @JsonProperty named attribute`; `@JsonIgnore property is not bound`;
  `default identifier conversion binds non-String id via convertValue`;
  `custom IdentifierConverter parse inverts the wire form`;
  `lid-only resource binds into identifier property`;
  `resource without id or lid omits the identifier property`;
  `explicit-null attribute binds null and omitted attribute keeps its default`;
  `unmapped resource attributes are ignored`;
  `fromResources binds homogeneous collection in order`;
  `fromResources validates every element type`;
  `omitted to-one relationship key is not bound`;
  `links-or-meta-only to-one relationship is not bound`;
  `NullLinkage on to-one binds null`;
  `collection linkage on to-one is a cardinality mismatch`;
  `empty collection linkage on to-many binds empty collection`;
  `empty collection linkage on to-many binds empty Set`;
  `empty collection linkage on to-many binds empty array`;
  `non-empty collection linkage on to-many binds List`;
  `non-empty collection linkage on to-many binds Set`;
  `non-empty collection linkage on to-many binds array`;
  `NullLinkage on to-many is a cardinality mismatch`;
  `single linkage on to-many is a cardinality mismatch`;
  `empty collection linkage on to-one is a cardinality mismatch`;
  `NullLinkage on Optional to-one binds empty Optional`;
  `SingleLinkage on Optional to-one binds present Optional`;
  `resource type mismatch is RESOURCE_TYPE_MISMATCH at /type`;
  `unregistered to-one relationship target is UNSUPPORTED_RELATIONSHIP_TARGET`;
  `unregistered to-many relationship target is UNSUPPORTED_RELATIONSHIP_TARGET`;
  `identifier parse exception is IDENTIFIER_CONVERSION_FAILED at /id`;
  `identifier parse returning null is IDENTIFIER_CONVERSION_FAILED`;
  `identifier coercion failure is IDENTIFIER_CONVERSION_FAILED`;
  `absent required creator property is MISSING_CREATOR_INPUT`;
  `creator throwing during instantiation is MISSING_CREATOR_INPUT`;
  `attribute value that cannot coerce is UNSUPPORTED_ATTRIBUTE_VALUE`;
  `explicit-null attribute into primitive property is UNSUPPORTED_ATTRIBUTE_VALUE`;
  `binder never sees document included resources`.
- Adapter-local exclusions by exact name: `custom deserializer applies to attribute value`;
  `naming strategy renames bound attribute keys`; `mix-in attribute name is honored`;
  `JavaType entry points bind resource and collection`;
  `registered linkage mapper binds to-one single linkage and to-many collection`;
  `mapper receives Optional-unwrapped to-one type and collection to-many type`;
  `NullLinkage and empty linkage short-circuit without invoking the mapper`;
  `cardinality is enforced before the mapper is invoked`;
  `mapper exception is reported as LINKAGE_MAPPING_FAILED`;
  `mapper returning null binds null property`.
- Capability-tagged codec documents from Phase 2.12 are optional inputs after that milestone lands;
  they are not a hard prerequisite. This milestone does not force every wire fixture into DTO
  binding. Typed-envelope catalogs remain Phase 2.26.

## Deliverables

- Move Jackson-neutral flat DTOs and reusable expected values for the closed shared test names into
  `jsonapi-java-test-fixtures`, with `@NullMarked` package-info, accurate `@Nullable` on
  null-bearing members per ADR-009, and Gradle dependencies on `jsonapi-java-core`, annotations,
  Phase 2.11 common contracts, and shared `jackson-annotations` only (no major-specific
  databind/core APIs).
- Add shared flat-binding scenarios for exactly those closed names with expected DTO values and
  stable common diagnostics.
- Refactor Jackson 3 `ResourceBinderSpec` to consume the catalog while retaining the named
  adapter-local cases.
- Document capability selection and integrity rules so later Jackson 2 binder suites must run every
  applicable shared scenario and explain every major-specific exclusion.
- Use `module-docs` for the `jsonapi-java-test-fixtures` domain-read package map and agent notes.

## Non-goals

- Typed domain envelope catalogs; Phase 2.26 owns them.
- Making every codec fixture DTO-bindable.
- Graph hydration, relationship injection, persistence lookup, or PATCH fixtures.
- Sharing `JavaType`, mapper, or custom-deserializer implementations across Jackson majors.

## Implementation boundaries

- Shared DTOs and expectations depend on annotations, core, and common Jackson contracts but import
  no major-specific databind/core API.
- Binder expectations remain resource-relative; `included` is never read for relationship fields.
- Identifier primary data is out of binder scope unless listed above; do not invent new
  dual-interpretation binder fixtures.

## Test strategy

- Run each shared flat-binding scenario through Jackson 3 and compare complete values, null/presence
  states, and diagnostics.
- Verify changes to `included` never alter primary DTO relationship fields.
- Add catalog tests for unique ids, declared binder capabilities, and explicit exclusions.

## Acceptance criteria

- [ ] Exactly the closed shared `ResourceBinderSpec` test names are present in the shared catalog
      without major-specific production imports, and the named adapter-local exclusions remain local.
- [ ] Jackson 3 `ResourceBinderSpec` consumes the catalog for those shared names and retains only
      the named adapter-local cases locally.
- [ ] Shared expectations preserve missing/null/linkage cardinality and never read `included`; new
      Java fixture packages are `@NullMarked` with accurate `@Nullable` per ADR-009.
- [ ] Catalog integrity prevents an adapter from omitting an applicable shared case; the canonical
      `module-docs` checklist passes for `jsonapi-java-test-fixtures` domain-read docs.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
