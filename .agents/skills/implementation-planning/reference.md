# Implementation planning reference

Use this file with [SKILL.md](SKILL.md). `AGENTS.md` remains authoritative for lifecycle,
canonical ownership, and change-scope completion gates.

## Plan file template

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
- [ ] Applicable completion gates from `AGENTS.md` pass in their prescribed order.
```

Replace `Module` with `Scope` for repository-wide work; use both only when established local format
requires it. Omit a section only when it adds no implementation value. Feature plans include the
applicable `docs/conformance.md` checklist update.

Use only `Not started` or `In progress`, never `Complete`. `Dependencies` are hard execution-order
prerequisites expressed as relative Markdown links to live plan files, or `None`; linked live plans
block implementation. Parallel-safe plans have no edge. Never use Linear, Outlook, deleted plans,
bare titles, or path stems as dependencies.

Include only gates applicable under `AGENTS.md`. In particular:

- Spotless-covered changes retain `./gradlew spotlessApply` then `./gradlew spotlessCheck` before
  the build. Spotless is already wired to `check`; preserve rather than change that wiring.
- Only module production/test sources matching `jsonapi-java-*/src/**` require Sonar;
  `build-logic/src/**` is build configuration, not Sonar scope.
- Source completion requires both a passing Sonar Quality Gate and a separate authenticated Issues
  API result of zero unresolved new-code issues. CI runs analysis but does not perform that API
  check. Without `SONAR_TOKEN`, report the blocker; CI must pass and the API check must still run
  separately before completion.

Use descriptive identity, not phase numbers or work-item IDs. Work items are optional traceability
only; otherwise record explicit coordination unsync. Do not copy ticket prose as engineering truth.
Acceptance criteria must be binary, independently verifiable, and collectively prove the goal, not
vague outcomes, implementation steps, or unbounded completeness claims. Never create a plans index.

## Decompose oversized work

Use the execution-unit rule in [SKILL.md](SKILL.md#choose-an-execution-unit), not conceptual or
numeric splitting alone.

1. Create coherent increments that each leave the repository buildable and require their own
   implementation/review context or module landing. Give each its own goal, boundaries, tests, and
   acceptance criteria.
2. Preserve real relative-link dependencies among surviving plans; leave parallel work unlinked.
   Put shared foundations in the earliest plan needing them and create a prerequisite only when it
   has independent acceptance evidence.
3. Replace, rather than retain as an umbrella, the `Not started` original. Finalize in this order:
   replacement plans exist; every replacement passes design and plan review; then find all incoming
   dependency and meaningful in-body references by filename, link, or relevant title across
   surviving `.agentWork/plans/*.md`.
4. If any affected dependent is `In progress`, stop and retain the original. For each affected
   `Not started` dependent, retarget real prerequisites to replacements or remove a no-longer-real
   prerequisite; never substitute Linear, Outlook, or historical prose.
5. Run new fresh `implementation-design-review` then `implementation-plan-review` on every modified
   dependent using planning's existing two-re-review caps. This is normal refinement, not a new
   review stage. If any review is Blocked or exhausts its loop, retain the original.
6. Mechanically verify zero surviving references to the superseded original, then and only then
   delete it.

Use descriptive filenames and ask before choosing among materially different naming schemes.
Explain the split through goals, dependencies, and non-goals rather than copied Vision/Outlook
narrative.

## Nullness and module-docs hooks

For Java public API work involving null-bearing types, require JSpecify `@NullMarked` packages and
accurate `@Nullable` decoration per ADR-009. When work adds a module or changes public packages,
entry points, validate/read flows, non-goals, or agent-relevant invariants, include the `module-docs`
skill as a deliverable and its canonical checklist as an acceptance criterion; reference rather
than duplicate that checklist.
