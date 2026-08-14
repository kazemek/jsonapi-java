# Milestone planning reference

Read this file when writing or decomposing milestone files. The procedure spine and execution-unit
rule live in [SKILL.md](SKILL.md).

## Milestone file template

Create or update files under `.agentWork/milestones/` using this shape:

```markdown
# <Phase and milestone title>

> **Module:** `<module>`
> **Dependencies:** <specific milestones or `None`>
> **Status:** Not started
> **Work item:** <optional identifier, e.g. KAZ-19>

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
- [ ] When the milestone changes production/test sources or build configuration: `./gradlew clean
      build` passes.
- [ ] When the milestone changes Spotless-covered files (`.java`, `.groovy`, `.kt`, `.gradle.kts`)
      or the formatter configuration: Spotless passes (`./gradlew spotlessApply` then
      `./gradlew spotlessCheck`).
- [ ] When the milestone changes production/test sources: Sonar Quality Gate passes; if
      `SONAR_TOKEN` is unavailable, report Sonar blocked and that CI must still pass the gate.
```

Include only the completion gates applicable to the milestone's scope per the change-scope gate
tiers in `AGENTS.md`; docs-only and workflow-only milestones omit the inapplicable gates.

Replace `Module` with `Scope` for repository-wide work; do not use both unless the established local
format requires both. Omit a section only when it adds no implementation value. For feature
milestones, include the relevant conformance-checklist update required by `docs/conformance.md`.

`Work item` is optional traceability metadata only. Omit the line when there is no identifier.
Do not put work-item identifiers in filenames or paths. Do not copy Linear (or other tracker)
ticket prose into Research and constraints as engineering truth. Dependencies list repository
milestones or `None`; Outlook and Linear never satisfy dependencies.

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
2. Give each increment its own milestone file, goal, boundaries, tests, and acceptance criteria.
3. Preserve real dependencies; allow independent increments to remain parallel. Outlook never
   appears as a dependency.
4. Put shared decisions or foundations in the earliest milestone that needs them. Create a separate
   prerequisite only when it has independent acceptance evidence.
5. Rework a not-started oversized milestone into implementable files. Do not leave an umbrella
   document presented as an implementable milestone.
6. Use the repository's phase naming and numbering pattern. Ask before choosing among materially
   different numbering schemes.

Explain the split briefly in the resulting milestones through goals, dependencies, and non-goals
rather than duplicating Vision or Outlook narrative.

## Nullness and module-docs hooks

When planning Java public API work that introduces or changes null-bearing types (absent members,
null-preserving maps/lists, or factory methods that pass `null`), include acceptance criteria that
require JSpecify `@NullMarked` packages and accurate `@Nullable` decoration per ADR-009.

When a milestone adds a submodule or changes public packages, entry points, validate/read flows,
non-goals, or agent-relevant invariants:

- include module documentation per the `module-docs` skill as a deliverable;
- include an acceptance criterion that the canonical `module-docs` checklist passes;
- reference that skill instead of copying its README/package-info checklist into the milestone.
