# Implementation planning reference

Read this file when writing or decomposing plan files. The procedure spine and execution-unit
rule live in [SKILL.md](SKILL.md).

## Plan file template

Create or update files under `.agentWork/plans/` using this shape. Do not create
`.agentWork/plans/README.md`.

```markdown
# <Descriptive title>

> **Module:** `<module>`
> **Dependencies:** [Other Live Plan](other-live-plan.md)
> **Status:** Not started
> **Work item:** <optional identifier, e.g. KAZ-24>

## Goal

<One coherent, independently testable outcome.>

## Research and constraints

- <Repository or authoritative source link/path> — <implementation consequence>

## Deliverables

- <Concrete output, behavior, or contract>

## Non-goals

- <Nearby work explicitly excluded or deferred>

## Implementation boundaries

- <Affected entry points, packages, compatibility rules, or policy boundaries>

## Test strategy

- <Focused positive, negative, integration, or fixture evidence>

## Acceptance criteria

- [ ] <Binary, observable criterion>
- [ ] `<focused verification command>` passes.
- [ ] When the plan changes production/test sources or build configuration: `./gradlew clean
      build` passes.
- [ ] When the plan changes Spotless-covered files (`.java`, `.groovy`, `.kt`, `.gradle.kts`)
      or the formatter configuration: Spotless passes (`./gradlew spotlessApply` then
      `./gradlew spotlessCheck`).
- [ ] When the plan changes production/test sources: Sonar Quality Gate passes; if
      `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
```

`Status` is only `Not started` or `In progress`. Never write `Complete`.

`Dependencies` is relative Markdown links to other live plan files, or `None`. Never Linear IDs,
Outlook, deleted plans, bare titles, or bare path stems alone.

Example:

```markdown
> **Dependencies:** [Jackson 3 Presence-Aware PATCH Binding](jackson3-presence-aware-patch-binding.md), [Jackson 2 Document Reader](jackson2-document-reader.md)
```

Or:

```markdown
> **Dependencies:** None
```

Include only the completion gates applicable to the plan's scope per the change-scope gate
tiers in `AGENTS.md`; docs-only and workflow-only plans omit the inapplicable gates.

Replace `Module` with `Scope` for repository-wide work; do not use both unless the established local
format requires both. Omit a section only when it adds no implementation value. For feature
plans, include the relevant conformance-checklist update required by `docs/conformance.md`.

Use descriptive filenames and titles. Do not use phase numbers or work-item identifiers as
structural identity in filenames, titles, or `Dependencies`.

`Work item` is optional traceability metadata only. Omit the line when there is no identifier and
record an explicit unsynchronized-coordination note instead. Do not put work-item identifiers in
filenames or paths. Do not copy Linear (or other tracker) ticket prose into Research and
constraints as engineering truth. Outlook and Linear never satisfy dependencies.

Acceptance criteria must be independently verifiable and collectively prove the goal. Do not use
vague criteria such as "works correctly," implementation-step checklists, or unbounded completeness
claims.

## Decompose oversized work

Decompose only when a genuine execution/review boundary exists (see Choose an execution unit in
[SKILL.md](SKILL.md)). Prefer the largest coherent unit that can still be implemented and reviewed
in one context. Conceptual breakdown of a capability map is not by itself a decompose operation.
Numeric deliverable and acceptance-criteria bounds are heuristics, not an automatic split.

1. Identify coherent increments that each leave the repository buildable **and** that each need a
   separate implementation/review context or module landing.
2. Give each increment its own plan file, goal, boundaries, tests, and acceptance criteria.
3. Preserve real dependencies as relative Markdown links among surviving live plans; allow
   independent increments to remain parallel. Outlook never appears as a dependency.
4. Put shared decisions or foundations in the earliest plan that needs them. Create a separate
   prerequisite only when it has independent acceptance evidence.
5. Replace a not-started oversized plan with implementable files. After every replacement has a
   design-review Pass and a plan-review Pass, delete the superseded original. Do not leave an
   umbrella document presented as an implementable plan.
6. Use descriptive filenames. Ask before choosing among materially different naming schemes.

Explain the split briefly in the resulting plans through goals, dependencies, and non-goals
rather than duplicating Vision or Outlook narrative.

## Nullness and module-docs hooks

When planning Java public API work that introduces or changes null-bearing types (absent members,
null-preserving maps/lists, or factory methods that pass `null`), include acceptance criteria that
require JSpecify `@NullMarked` packages and accurate `@Nullable` decoration per ADR-009.

When a plan adds a submodule or changes public packages, entry points, validate/read flows,
non-goals, or agent-relevant invariants:

- include module documentation per the `module-docs` skill as a deliverable;
- include an acceptance criterion that the canonical `module-docs` checklist passes;
- reference that skill instead of copying its README/package-info checklist into the plan.
