# Implementation planning reference

Use this file with [SKILL.md](SKILL.md). `AGENTS.md` remains authoritative for lifecycle,
canonical ownership, and change-scope completion gates. Finding severity and stage ownership live
in [../review-findings.md](../review-findings.md).

## Plan file template

```markdown
# <Descriptive title>

> **Module:** `<module>`
> **Dependencies:** [Other Live Plan](other-live-plan.md)
> **Status:** Not started
> **Work item:** <optional external work-item identifier>

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
block implementation. Parallel-safe plans have no edge. Never use external work-item IDs, Outlook,
deleted plans, bare titles, or path stems as dependencies.

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

## Review epochs

Canonical owner of automatic review budgets and user-authorized continuation for planning reviews.

### Ledger

For each plan basename and review kind (`design` or `plan`), create or update the fixed ledger path
from `.agents/skills/implementation-design-review/reference.md`:

```markdown
# Review epoch ledger: <plan title>

- **Kind:** design | plan
- **Plan identity:** `<basename>`
- **Plan path (latest):** `<path>`
- **Current epoch:** <n>
- **Budget per epoch:** 1 initial review + at most 1 automatic re-review
- **Attempts used in current epoch:** <m>
- **Epoch status:** active | exhausted | passed
- **Authorizations:**
  - Epoch 1: automatic on first planning review of this identity
  - Epoch <n>: user <ISO-8601> — "<verbatim authorizing instruction>"
```

Identity is the plan basename. Renaming the file updates **Plan path (latest)** only; it does not
create a new ledger or reset counters. Editing the plan body does not reset counters. Starting a
new chat/session does not reset counters while the ledger exists under `.agentWork/.session/`.

### Budget and attempts

Each epoch allows one initial review attempt and, after `Changes required`, at most one automatic
fresh re-review after the planner fixes all known in-scope findings. Two failed attempts with
remaining gate findings exhaust the epoch. The orchestrator must not autonomously open a new epoch.

Before each new attempt, if fixed-path artifacts already exist, copy them into
`.agentWork/.session/archive/` using the archive naming in the design-review reference, then replace
the fixed paths with the new attempt. Keep the ledger and archives when an epoch exhausts.

### User-authorized continuation

After exhaustion, stop and return control to the user. A later **explicit** user instruction may
authorize another bounded epoch for the same `Not started` plan and review kind. Record the
verbatim instruction under **Authorizations**, increment **Current epoch**, reset attempts to 0,
set status `active`, and preserve prior archives and ledger history.

Do not treat bare `continue`, generic “try again,” or plan edits as authorization. Do not offer
epoch continuation for `In progress` plans or to rewrite a frozen implementation contract.

### Anti-reset rules

Forbidden ways to obtain a fresh budget:

- editing the plan;
- renaming the plan;
- restarting the session;
- deleting or ignoring the ledger;
- artificial split, replacement, or decomposition solely to mint new counters.

Genuine decomposition remains valid when [Choose an execution unit](SKILL.md#choose-an-execution-unit)
is satisfied; each genuine new plan identity receives its own ledger. After an exhausted epoch,
refuse fake splits that exist only to reset review budgets.

## Dependency-aware review waves

When more than one plan is in scope, derive the DAG from each plan's `Dependencies` links.

A plan is **accepted** only when it has both a design-review official `Pass` and a plan-review
`Pass` in the current planning effort. A plan is **reviewable** only when every linked dependency
is accepted, or its dependencies are `None`.

Review only the ready wave. Independent plans in the same wave may run concurrently. Do not run a
dependent plan's full design or plan review while a prerequisite is still unaccepted or is being
materially revised.

If an upstream plan changes materially before a downstream plan is accepted:

1. Retain useful downstream findings already collected (archives / prior artifacts).
2. Do not repeatedly re-review the downstream plan until prerequisites are accepted again.
3. When the downstream plan becomes eligible, apply retained findings and run a fresh review only
   as the epoch budget allows.

Put shared architecture decisions in the earliest prerequisite plan. Do **not** add a mandatory
extra plan-set architecture review layer; wave-ordered design review of foundations replaces that
ceremony.

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
   replacement plans exist; every replacement is accepted (design Pass and plan Pass) in dependency
   waves; then find all incoming dependency and meaningful in-body references by filename, link, or
   relevant title across surviving `.agentWork/plans/*.md`.
4. If any affected dependent is `In progress`, stop and retain the original. For each affected
   `Not started` dependent, retarget real prerequisites to replacements or remove a no-longer-real
   prerequisite; never substitute external work-tracker metadata, Outlook, or historical prose.
5. Run wave-ordered fresh `implementation-design-review` then `implementation-plan-review` on every
   modified dependent using planning's epoch budgets. This is normal refinement, not a new review
   stage. If any review is Blocked or exhausts its epoch, retain the original.
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

## Scenario expectations

These cases are normative expectations for the planning/review rules above.

### Severity and stage ownership

1. Contradictory ownership/source-of-truth → `Blocking`; design fails.
2. Conceptually preserved API missing one existing constructor → usually `Required`; design may
   Pass; plan review requires correction before implementation.
3. Preferring a different class name → `Advisory`; no gate failure.
4. Missing detail that forces an unresolved architectural choice → escalate to `Blocking`; never
   hide under `Required`.

### Exhaustiveness

5. Four independent blocking problems → report all four in one pass.
6. No blockers but three completeness issues → design Pass with `Required` carry-forward.

### Review bounds

7. Initial design finds blockers; one fresh re-review Passes → continue to plan review.
8. Initial and automatic re-review both leave blockers → exhaust epoch; return control to user.
9. Explicit user authorization of another epoch → preserve history; new bounded budget.
10. Edit or rename without authorization → counters unchanged.
11. New chat/session → counters unchanged while the ledger remains.
12. Fake split solely for new counters → refuse.
13. Genuine new execution-unit boundary after exhaustion → genuine decomposition still allowed.

### Dependency waves

14. B depends on unaccepted A → do not fully review B yet.
15. B and C depend only on accepted A → B and C may review in parallel.
16. D depends on C → D waits until C is accepted.
17. Downstream had early useful findings; upstream then changed → retain findings; defer re-review
    until prerequisites stabilize.

### Implementation freeze

18. Plan is `In progress` → review-epoch continuation must not rewrite the frozen contract.
