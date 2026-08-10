# Phase 2.26 — Shared Typed Envelope Read Test Fixtures

> **Scope:** `jsonapi-java-test-fixtures` / jackson3 `DomainDocumentReaderSpec`  
> **Dependencies:** Phases 2.10, 2.11, and 2.14  
> **Status:** Not started

## Goal

Extract one version-neutral typed-envelope scenario catalog from Phase 2.10 tests for cross-major
parity without forcing every codec fixture into DTO binding.

## Research and constraints

- Phase 2.10 `DomainDocumentReaderSpec` uses a capability-relevant subset of canonical documents
  plus named binding variants. Applicability is owned entirely by this catalog’s explicit allow-list
  of fixture ids / named binding variants; it does not require a Phase 2.12 `DOMAIN_BIND` capability
  flag. Phase 2.12 documents may be reused when listed, but 2.12 is not a hard dependency.
- Phase 2.14 owns flat-binding DTOs; this milestone composes them into envelope scenarios.
- [ADR-011](../../docs/adr/011-flat-dto-read-binding.md) — included resources bind independently
  and are never injected into relationships.
- Closed shared `DomainDocumentReaderSpec` test names:
  `binds a single-resource document into a flat DTO envelope`;
  `binds a homogeneous resource collection in wire order`;
  `binds a heterogeneous collection through the registry`;
  `preserves explicit null data as NullData`;
  `preserves absent data on a meta-only document`;
  `passes through identifier primary data without DTO binding`;
  `preserves errors without binding anything`;
  `preserves jsonapi object, nullable links, and additional members`;
  `absent included stays null while present-empty included is a non-null empty IncludedResources`;
  `binds included resources preserving wire order with identity lookup`;
  `compound shared identity binds one included DTO reachable from both primary resources`;
  `shared identity yields one DTO instance reachable from both id and lid keys`;
  `fromDocument fails fast on duplicate included identities`;
  `unregistered resource-shaped primary fails at the document pointer with null resourceClass`;
  `unregistered included type fails at the included index`;
  `duplicate registry type names fail at build with the later registrant`;
  `registration rejects missing, empty, and invalid resource annotations`;
  `binder failures surface with the document pointer joined to the binder path`;
  `root-level binder failures join to the document pointer without a trailing slash`;
  `cyclic linkage keeps relationship fields as identifiers while included DTOs stay separate`;
  `independent envelopes sharing linkage never inject included DTOs`;
  `envelope collections are mutation-safe`.
- Adapter-local exclusions by exact name:
  `metaAs returns null for both overloads when meta is absent`;
  `metaAs converts via the caller-mapper module on both entry paths and both overloads`;
  `incompatible metaAs target is UNSUPPORTED_ATTRIBUTE_VALUE at /meta`;
  `JavaType registrations bind through the same registry gate`;
  `builder-based domainDocumentReader overloads derive readers that bind identically`;
  `custom linkage mappers apply to primary and included resources`;
  `caller-owned stream and parser remain open on success and failure`;
  `malformed input stays JsonApiDocumentReadException with category and location`;
  `validation failures keep the originating rule code`.

## Deliverables

- Add shared typed-envelope scenarios for exactly the closed shared test names above, selecting
  listed Phase 2.12 documents or named binding variants with expected envelope values, registration
  outcomes, included order, dual id/lid lookup, and no-injection proofs.
- Explicitly allow-list applicable fixture ids / named binding variants inside this catalog and
  retain the named adapter-local exclusions.
- Refactor Jackson 3 `DomainDocumentReaderSpec` to consume the catalog while retaining adapter-local
  configuration cases.
- Add catalog integrity tests for unique ids, resolvable documents/variants, and exclusions.
- Use `module-docs` for the `jsonapi-java-test-fixtures` envelope-read package map and agent notes.

## Non-goals

- Flat binder extraction (Phase 2.14) or Jackson 2 envelope implementation (Phase 2.22).
- Graph hydration, relationship injection, or PATCH fixtures.
- Extending Phase 2.12 capability metadata with a domain-bind flag.
- Re-cataloging malformed/validation codec failures owned by Phase 2.12.

## Implementation boundaries

- Envelope scenarios reuse common envelope/domain-data contracts and Phase 2.14 DTOs.
- Absent versus empty `included` remains distinct; identifier primary data stays core values;
  errors never bind.
- Applicability is this catalog’s allow-list, not a codec-fixture capability tag.

## Test strategy

- Parameterize the closed shared test names through Jackson 3 and compare complete values and
  diagnostics.
- Catalog integrity rejects omitted applicable cases and undocumented exclusions.

## Acceptance criteria

- [ ] Exactly the closed shared `DomainDocumentReaderSpec` test names are cataloged, with an explicit
      allow-list of fixture ids / named binding variants and the named adapter-local exclusions.
- [ ] Jackson 3 `DomainDocumentReaderSpec` consumes the catalog for those shared names.
- [ ] Shared expectations preserve registration, included order/identity, pointer composition, and
      the no-injection boundary; catalog integrity rejects omitted applicable cases.
- [ ] The canonical `module-docs` checklist passes for `jsonapi-java-test-fixtures` envelope-read
      docs.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] Sonar Quality Gate passes; if `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI
      must still pass the gate.
