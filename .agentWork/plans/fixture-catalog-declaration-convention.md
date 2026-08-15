# Fixture Catalog Declaration Convention

> **Module:** `jsonapi-java-test-fixtures`
> **Dependencies:** None
> **Status:** In progress
> **Work item:** KAZ-18

## Goal

Establish one universal declaration layout for every explicit Java fixture catalog—scenarios are
declared in the `*Scenarios` catalog class (private helpers in the same package allowed)—document
that rule for future catalogs including PATCH/`domainpatch`, and migrate the codec `cases/`
builders into that layout without changing retrieval APIs, scenario ids, or wire fixtures.

## Research and constraints

- [`jsonapi-java-test-fixtures/README.md`](../../jsonapi-java-test-fixtures/README.md) — owns
  package roles, retrieval via `JsonApiFixtures` / `FixtureCatalog`, stable ids, catalog-spec
  invariants, and the agent extension workflow. The declaration-layout rule belongs here; do not
  invent a second owner.
- Domain pattern (target) — `DomainWriteScenarios`, `DomainReadScenarios`, `CompoundWriteScenarios`,
  `SparseFieldsetScenarios`, and `EnvelopeReadScenarios` already declare scenarios inline in the
  catalog class. Keep that layout; do not extract them into per-scenario top-level builder classes.
- Codec pattern (migrate away) — `CodecScenarios` / `AmbiguousPrimaryDataScenarios` currently list
  one public builder class per corpus entry under
  `io.github.kazemek.jsonapi.testfixtures.codec.cases` (~26 builders). Entries bind to named
  wire-corpus paths with `manifest.json` / ambiguous-manifest order and catalog-spec bijection.
  Manifest order and bijection stay; the dedicated `cases/` package does not.
- Negative codec — `NegativeCodecScenarios` loads `negative-manifest.json` via JSON-P. Out of scope
  for this declaration-layout rule; README already documents that loader separately.
- [`jackson3-presence-aware-patch-binding.md`](jackson3-presence-aware-patch-binding.md) — pins
  `domainpatch`, `PatchScenarios` / `JsonApiFixtures.patch()`, grow-by-addition, and full-catalog
  coverage. This plan requires future PATCH/`domainpatch` to use the same universal inline
  declaration; do not reopen PATCH inventory or binder design here.
- [ADR-007](../../docs/adr/007-module-boundaries.md) / [ADR-010](../../docs/adr/010-architectural-tests.md)
  — test-fixtures stay Jackson-major-neutral; ArchUnit on main bytecode unchanged. No ADR required:
  contributor-convention documentation plus an internal catalog-source refactor.
- Maintainer choice — prefer one universal inline layout over documenting a codec-vs-domain split,
  accepting larger codec catalog sources (mitigated by private helpers) rather than extracting all
  domain catalogs into `cases/` packages.

## Chosen convention

| Catalog category | Declaration layout |
|---|---|
| Explicit Java catalogs (`CodecScenarios`, `AmbiguousPrimaryDataScenarios`, `DomainWrite*`, `DomainRead*`, `CompoundWrite*`, `SparseFieldset*`, `EnvelopeRead*`, future `PatchScenarios` / `domainpatch`) | Declare scenarios in the `*Scenarios` catalog class; private helpers in the same package are allowed; grow by addition where that catalog’s rules say so; no mandatory per-scenario top-level builder class or `cases/` package |
| JSON-backed negative codec | Unchanged manifest loader (documented separately; not part of this layout rule) |

## Deliverables

- Apply the `module-docs` skill for `jsonapi-java-test-fixtures` when adding/updating the
  declaration-layout invariant (README and any package-info touched by removing `codec.cases`);
  follow that skill’s checklist rather than ad-hoc Markdown edits.
- Replace the **Declaration layout** subsection under “For contributors / agents” in
  `jsonapi-java-test-fixtures/README.md` so it states the universal inline rule above (including
  future PATCH/`domainpatch`) and excludes the negative codec loader from that rule.
- Retarget every other README reference to the old codec-vs-domain category split—including the
  Non-goals PATCH sentence that currently names a “domain-style row”—so PATCH/`domainpatch`
  points at the universal inline rule (or `#declaration-layout`) without naming removed rows.
- Update the packages table: remove the `codec.cases` row (or restate codec without a separate
  cases package) and cross-link Declaration layout from the remaining codec package row.
- Migrate `CodecScenarios` and `AmbiguousPrimaryDataScenarios` off
  `io.github.kazemek.jsonapi.testfixtures.codec.cases`: fold each builder’s `scenario()` body into
  the catalog class or into private helpers in `…testfixtures.codec` (not a `cases` subpackage);
  delete the `codec.cases` sources and `package-info.java`; keep catalog list order identical to
  today’s manifest/corpus order.
- Leave `Scenario`, `FixtureCatalog`, `JsonApiFixtures`, negative-manifest loading, adapter
  dispatch, full-catalog coverage assertions, and domain-style catalog sources unchanged (do not
  extract domain catalogs into `cases/` packages).


## Non-goals

- Changing `Scenario`, `FixtureCatalog`, or `JsonApiFixtures` semantics or public retrieval surface.
- Changing the JSON-backed negative codec manifest loader or its catalog spec.
- Changing adapter dispatch rules or full-catalog coverage assertions.
- Extracting domain-style catalogs into per-scenario `cases/` packages.
- Implementing the PATCH catalog, inventory, or Jackson binders (owned by
  [`jackson3-presence-aware-patch-binding.md`](jackson3-presence-aware-patch-binding.md)).
- New ADRs, Vision updates, or Outlook edits.
- Renaming scenario ids or rewriting wire fixtures under `fixtures/`.

## Implementation boundaries

- Primary touch: `jsonapi-java-test-fixtures` README plus
  `jsonapi-java-test-fixtures/src/main/java/io/github/kazemek/jsonapi/testfixtures/codec/**`
  (catalog classes, delete `codec/cases/**`, update `codec/package-info.java` if it references
  `cases`).
- Do not rename scenario ids, alter expected models/outcomes/comparison policies, or move wire
  fixtures under `fixtures/`.
- Optional private helper types may live in `…testfixtures.codec` only if keeping them as
  top-level types is clearer than methods on the catalog class; they must not form a public
  `cases` (or equivalent) builder package advertised in the README packages table.

## Test strategy

- After migration, `CodecScenariosCatalogSpec`, ambiguous-primary-data catalog specs, and any
  adapter suites that consume codec fixtures must stay green with unchanged scenario ids and
  expected paths/models.
- Domain `*ScenariosCatalogSpec` suites remain green without source moves.
- Sanity-check README: one universal inline rule; no `codec.cases` package row; PATCH named as
  following the same rule; negative corpus still documented separately.

## Acceptance criteria

- [ ] `jsonapi-java-test-fixtures/README.md` states one universal inline declaration rule for all
      explicit Java catalogs (including future PATCH/`domainpatch`), notes that the negative codec
      loader is outside that layout rule, and contains no remaining references to a codec-vs-domain
      category split or a “domain-style row” (including Non-goals).
- [ ] The `codec.cases` package is removed; `CodecScenarios` and `AmbiguousPrimaryDataScenarios`
      declare their entries without importing a `cases` subpackage; packages-table / package-info
      no longer advertise per-corpus builder classes under `codec.cases`.
- [ ] The `module-docs` skill checklist passes for every path that skill updates for this work.
- [ ] Scenario ids, payloads/expected models, comparison policies, retrieval APIs, negative-manifest
      loading, adapter dispatch, and wire fixtures under `fixtures/` are unchanged.
- [ ] `./gradlew :jsonapi-java-test-fixtures:test` passes.
- [ ] `./gradlew spotlessApply` then `./gradlew spotlessCheck` pass (Java sources change).
- [ ] `./gradlew clean build` passes.
- [ ] The `sonar-quality-gate` skill’s Quality Gate wait and Issues API script exit 0 (`src/**`
      changes).
