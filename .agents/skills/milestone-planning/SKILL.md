---
name: milestone-planning
description: Creates, refines, or decomposes research-backed implementation milestones under `.agentWork/milestones/`. Use only when the user explicitly requests milestone planning, refinement, or breakdown.
disable-model-invocation: true
---

# Milestone Planning

Produce permanent, implementation-ready milestone files. Planning ends when the milestone files and milestone index are written; do not implement the planned feature.

## Resolve the operation

Determine whether the request is:

- **Create:** no existing milestone covers the requested outcome.
- **Refine:** one not-started milestone exists but needs clearer evidence, boundaries, tests, or acceptance criteria.
- **Decompose:** the requested or existing scope exceeds the size gate.

Resolve the target phase and milestone unambiguously. Ask the user only when naming, ordering, or scope has materially different valid choices that repository evidence cannot resolve.

## Explore before writing

Read these repository sources in order:

1. `AGENTS.md`, `docs/vision.md`, and `.agentWork/milestones/README.md`.
2. The target milestone when refining or decomposing. Read dependency or adjacent milestones only
   when their contracts can constrain ordering, compatibility, or scope; use index metadata to
   avoid opening unrelated milestones.
3. `settings.gradle.kts` and each affected module's `README.md`.
4. `package-info.java` for packages likely to change.
5. Only ADRs and conformance sources linked by the vision, module README, target milestone, or directly implicated code.
6. Narrow production and test files needed to validate feasibility and boundaries.

Do not scan the whole repository first. Search for overlapping milestones, existing APIs, naming conventions, diagnostics, fixtures, and test patterns before proposing new concepts.

## Research the contract

Research enough to replace assumptions with implementable constraints:

- Prefer official specifications, primary project documentation, standards, and source code.
- Use current external research only when repository evidence is insufficient or the milestone depends on third-party behavior.
- Record only sources and conclusions that constrain scope, behavior, compatibility, or testing.
- Distinguish confirmed requirements from proposed policy.
- Never copy large source passages into a milestone; link to the source and state its implementation consequence.

If research exposes a vision conflict, flag it before writing the implementation contract. If the divergence is intentional, include the required vision update in scope or make it a prerequisite. Identify consequential, hard-to-reverse decisions that require a new or updated ADR; do not silently settle them in milestone prose.

## Respect milestone lifecycle

- A `Not started` milestone may be refined in place or replaced by smaller milestones.
- Once implementation has started, treat its milestone as fixed. Status, commits, code changes, or user context may establish that implementation started.
- Never rewrite a completed or implementation-started milestone to describe new work. Create a follow-up milestone with an explicit dependency instead.
- If lifecycle state is uncertain and changes whether a file may be edited, ask the user.

Checked acceptance criteria are delivery claims, not planning evidence. Do not mark criteria complete while planning.

## Enforce the size gate

Every implementable milestone must satisfy all of these:

- One coherent outcome expressible in one sentence.
- One principal capability and normally one primary module or layer.
- Independent value and verification at its completion.
- A narrow discovery set that does not require unrelated package or module context.
- At most five deliverables and eight acceptance criteria, including repository completion gates.
- Feasible for one focused coding-agent task and one reviewable commit.

Cross-module work is allowed only when the integration itself is the single outcome. Markdown length is not a size measure.

Split the work when any of these are independently deliverable:

- foundations, public API, runtime behavior, adapters, migration, or hardening;
- changes to separate modules that do not need to land atomically;
- multiple architectural decisions with distinct consequences;
- unrelated test or fixture suites;
- acceptance groups that could pass and be useful while another group remains unimplemented.

When uncertain, prefer the smaller independently useful milestone.

## Decompose oversized work

1. Identify the smallest end-to-end increments that each leave the repository buildable.
2. Give each increment its own milestone file, goal, boundaries, tests, and acceptance criteria.
3. Preserve real dependencies; allow independent increments to remain parallel.
4. Put shared decisions or foundations in the earliest milestone that needs them. Create a separate prerequisite only when it has independent acceptance evidence.
5. Rework a not-started oversized milestone into implementable files. Do not leave an umbrella document presented as an implementable milestone.
6. Use the repository's phase naming and numbering pattern. Ask before choosing among materially different numbering schemes.

Explain the split briefly in the resulting milestones through goals, dependencies, and non-goals rather than duplicating a roadmap narrative.

## Write the milestone files

Create or update files under `.agentWork/milestones/` using this shape:

```markdown
# <Phase and milestone title>

> **Module:** `<module>`
> **Dependencies:** <specific milestones or `None`>
> **Status:** Not started

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

Replace `Module` with `Scope` for repository-wide work; do not use both unless the established local format requires both. Omit a section only when it adds no implementation value. For feature milestones, include the relevant conformance-checklist update required by `docs/vision.md`.

Acceptance criteria must be independently verifiable and collectively prove the goal. Do not use vague criteria such as "works correctly," implementation-step checklists, or unbounded completeness claims.

When planning Java public API work that introduces or changes null-bearing types (absent members,
null-preserving maps/lists, or factory methods that pass `null`), include acceptance criteria that
require JSpecify `@NullMarked` packages and accurate `@Nullable` decoration per ADR-009.

When a milestone adds a submodule or changes public packages, entry points, validate/read flows,
non-goals, or agent-relevant invariants:

- include module documentation per the `module-docs` skill as a deliverable;
- include an acceptance criterion that the canonical `module-docs` checklist passes;
- reference that skill instead of copying its README/package-info checklist into the milestone.

## Synchronize and verify

After writing all milestone files:

1. Update `.agentWork/milestones/README.md` dependency order and milestone index. Every index entry
   must retain the canonical `milestone — module/scope — status` format.
2. Verify every link, phase identifier, dependency, scope/module, status, and command against the
   milestone files.
3. Reapply the size gate to each emitted milestone.
4. Confirm decomposed milestones do not overlap or omit requirements from the source request.
5. Confirm milestone prose links to rather than duplicates vision, ADR, conformance, and module documentation.
6. Report the files created or refined, research that materially changed the scope, and any decomposition rationale.
