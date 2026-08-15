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
- **Plan path:** `<path>`
- **Current epoch:** <n>
- **Budget per epoch:** 1 initial review + at most 1 automatic re-review
- **Attempts used in current epoch:** <m>
- **Review sequence in current epoch:** <r>
- **Epoch status:** active | blocked | exhausted | passed
- **Blocked reason:** <missing prerequisite, or "none">
- **Authorizations:**
  - Epoch 1: automatic on first planning review of this identity
  - Epoch <n>: <refine | exhausted-continuation | architectural-escalation> <ISO-8601> — "<verbatim instruction or reason>"
  - Resume: blocked-resume <ISO-8601> — "<verbatim later planning invocation>"
```

Identity is the plan basename and is fixed once any review ledger exists for that plan. Renaming the
plan file is allowed only before the first design or plan ledger is created. After a ledger exists,
do not rename the plan; basename-keyed ledgers, artifacts, carry-forward, and archives would
otherwise detach from the contract and reopen a reset-by-rename loophole. Editing the plan body does
not reset counters. Starting a new chat/session does not reset counters while the ledger exists under
`.agentWork/.session/`.

### Budget and attempts

Each epoch allows one initial review attempt and, after `Changes required`, at most one automatic
fresh re-review after the planner fixes all known in-scope findings. Two failed attempts with
remaining gate findings exhaust the epoch. The orchestrator must not autonomously open a new epoch
except for the single architectural-escalation transition below.

**Attempts used** counts only completed `Pass` or `Changes required` attempts. A `Blocked` verdict
does not consume budget.

**Review sequence** increments for every actual reviewer invocation in the current epoch, including
`Blocked`, `Pass`, and `Changes required`. It is independent of **Attempts used** and exists so
archives never collide when Blocked/resume retries reuse the same budget slot.

Before each reviewer invocation, if fixed-path artifacts already exist, copy them into
`.agentWork/.session/archive/` using the archive naming in the design-review reference (epoch +
review sequence), then replace the fixed paths. Keep the ledger and archives when an epoch
exhausts or blocks.

### Blocked suspend and resume

On a design or plan `Blocked` verdict:

1. Set epoch status to `blocked`.
2. Record **Blocked reason** (missing ADR/file/lifecycle fact, unavailable evidence, etc.).
3. Stop. Do not open a new epoch and do not reset attempts or review sequence.
4. Do not run the next review stage.

When the missing prerequisite becomes available, an **explicit later planning invocation** for the
same `Not started` plan may resume the same bounded epoch: set status `active`, clear **Blocked
reason**, record a `blocked-resume` authorization line, and retry within the remaining budget. Do
not mint a fresh automatic budget. The resume increments **Review sequence** for the new invocation
but does not increment **Attempts used** until a `Pass` or `Changes required` completes. Bare
`continue` or plan edits alone are not resume authorization. `In progress` plans cannot use
blocked-resume to rewrite a frozen contract.

### Gate carry-forward

Unresolved gate findings must not disappear across attempts or epoch boundaries, subject to the
design-Blocking clear-on-Pass rule below.

#### Design gate carry-forward

After every design-review attempt that produces findings, and whenever a design epoch exhausts,
planning unions unresolved design gate findings into:

```text
.agentWork/.session/design-gate-carry-forward-<basename>.md
```

Sections:

- **Blocking** — every unresolved design `Blocking` finding (especially remaining at exhaust).
- **Required** — every unresolved design `Required` finding (including across Pass attempts that
  fail to rediscover them).

Before each fresh design attempt, planning must apply every known design **Blocking** carry-forward
item to the plan. Design reviewers stay fresh and isolated: they must not read prior artifacts or
carry-forward merely to clear findings.

**Design Blocking clear-on-Pass:** if that fresh exhaustive design review returns official `Pass`,
clear the pre-existing design **Blocking** carry-forward entries for that attempt. An independent
fresh design `Pass` is the verification that the remediated architecture is now acceptable. Do
**not** clear design **Required** on design Pass — those remain sticky into plan review.

If planning did not address a Blocking item, or the fresh review reports that item or any other
`Blocking` finding (`Changes required`), retain/update Blocking carry-forward; it remains gating.

#### Plan gate carry-forward

After every plan-review attempt that produces findings, and whenever a plan epoch exhausts,
planning unions unresolved plan gate findings into:

```text
.agentWork/.session/plan-gate-carry-forward-<basename>.md
```

Sections:

- **Blocking** — every unresolved plan `Blocking` finding.
- **Required** — every unresolved plan `Required` finding (including design Required carry-forward
  items not yet addressed).

Plan gate entries clear only when a later plan review has verified the plan addresses them (or
severity legitimately escalates and the higher-severity entry replaces the lower one). Never remove
a plan gate entry or a design **Required** entry merely because a later fresh reviewer omitted it.

### Opening a new epoch

Open a new bounded epoch (increment **Current epoch**, reset attempts to 0, reset review sequence to
0, set status `active`, preserve prior archives, ledger history, and gate carry-forward) only for:

1. **First review** of this identity — Epoch 1, automatic.
2. **Exhausted continuation** — after status `exhausted`, a later **explicit** user instruction
   authorizes another epoch for the same `Not started` plan and review kind. Record the verbatim
   instruction. Do not treat bare `continue`, generic “try again,” or plan edits as authorization.
   Before the first attempt of the new epoch, planning must read the final exhausted-epoch artifacts
   and the gate carry-forward, apply every unresolved gate finding to the plan (or retain it
   explicitly in carry-forward when it cannot yet be resolved), and keep those findings gating until
   a later reviewer verifies they are addressed. Explicit authorization to spend another budget does
   not waive unresolved findings from the exhausted epoch.
3. **User-requested Refine** — an explicit later `implementation-planning` Refine of a `Not started`
   plan whose prior design and/or plan epoch status is `passed`. The Refine invocation itself
   authorizes new epochs for the review kinds that must run again. Record the refine instruction.
   Planner edits inside an already-active, blocked, or exhausted loop do not open a new epoch.
4. **Architectural escalation** — when plan review reports a `Blocking` architectural finding and
   the design ledger status is `passed`, planning opens **one** new design epoch automatically,
   recorded as `architectural-escalation`, and re-runs design review before further plan review.
   If the design ledger is already `exhausted` or `blocked`, stop and require the matching
   exhausted-continuation or blocked-resume authorization instead. Do not auto-open further design
   epochs for the same escalation chain without a new Refine or exhausted-continuation authorization
   after that escalated epoch ends.

Do not offer epoch continuation for `In progress` plans or to rewrite a frozen implementation
contract. Status `blocked` resumes in-place; it does not open a new epoch.

### Anti-reset rules

Forbidden ways to obtain a fresh budget:

- editing the plan inside an active, blocked, or exhausted epoch;
- renaming the plan after any review ledger exists;
- restarting the session;
- deleting or ignoring the ledger or gate carry-forward;
- artificial split, replacement, or decomposition solely to mint new counters;
- treating exhausted-continuation authorization as a waiver of unresolved gate findings.

Genuine decomposition remains valid when [Choose an execution unit](SKILL.md#choose-an-execution-unit)
is satisfied; each genuine new plan identity receives its own ledger. After an exhausted epoch,
refuse fake splits that exist only to reset review budgets.

## Dependency-aware review waves

When more than one plan is in the current create/refine/decompose operation, derive the DAG from
each in-scope plan's `Dependencies` links.

A plan **in the current planning operation** is **accepted** only when it has both a design-review
official `Pass` and a plan-review `Pass` during this operation. Wave gating uses that current-effort
acceptance only for plans that this operation created, refined, or decomposed.

For a linked dependency:

- **In this operation and not yet accepted** → downstream is not reviewable yet.
- **In this operation and being materially revised** → downstream remains blocked until that change
  is accepted again.
- **Outside this operation, live, and not being materially revised** → treat as a stable external
  prerequisite for wave ordering; do not re-review it solely to unlock a dependent.
- **Outside this operation but known to be actively/materially changing** → downstream remains
  blocked until that change stabilizes (accepted or otherwise settled outside this cheap re-review
  path).

A plan in this operation is **reviewable** when every linked dependency satisfies one of the stable
cases above (or dependencies are `None`).

Review only the ready wave. Independent plans in the same wave may run concurrently. Do not run a
dependent plan's full design or plan review while an in-scope or materially changing prerequisite is
still unstable.

If an upstream plan in this operation changes materially before a downstream plan is accepted:

1. Retain useful downstream findings already collected (archives / prior artifacts / carry-forward).
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
9. Explicit user authorization of another epoch after exhaustion → preserve history; new bounded
   budget. Unresolved gate findings from the exhausted epoch must be remediated or remain gating.
10. Edit inside an active/blocked/exhausted epoch without authorization → counters unchanged. Rename
    after any review ledger exists → refuse (identity is frozen).
11. New chat/session → counters unchanged while the ledger remains.
12. Fake split solely for new counters → refuse.
13. Genuine new execution-unit boundary after exhaustion → genuine decomposition still allowed.
14. Design attempt reports Required B then Blocking A; re-review Passes without rediscovering B →
    B remains in the design gate carry-forward for plan review.
15. Design attempt 1 reports Blocking A; planning applies A; fresh attempt 2 returns Pass without
    mentioning A → clear A from design Blocking carry-forward; plan review may proceed. Design
    Required entries are not cleared by that Pass.
16. Planning does not address Blocking A, or fresh review finds A / another Blocking → Blocking
    carry-forward remains gating.
17. Explicit user-requested Refine of a previously passed `Not started` plan → new bounded epochs
    for the reviews that must run again; prior history preserved.
18. Plan review reports architectural Blocking after design `passed` → one automatic new design
    epoch (`architectural-escalation`); if design is already exhausted or blocked, stop for the
    matching authorization.
19. Design or plan attempt returns `Blocked` → epoch status `blocked`; later explicit planning
    invocation after the prerequisite is available resumes the same epoch without a new budget.
20. Two consecutive Blocked/resume cycles then Pass → each invocation gets a distinct archive via
    review sequence; Attempts used still counts only Pass/Changes required.
21. Epoch 1 exhausts with unresolved Required/Blocking C; user authorizes Epoch 2; fresh Pass omits
    C → design Blocking C clears only under the clear-on-Pass rule after remediation; design
    Required C and plan gate findings remain gating until verified addressed.

### Dependency waves

22. B depends on in-operation unaccepted A → do not fully review B yet.
23. B and C depend only on in-operation accepted A → B and C may review in parallel.
24. D depends on in-operation C → D waits until C is accepted.
25. B depends on live outside-operation A that is not being revised → A is a stable external
    prerequisite; do not re-review A solely to unlock B.
26. Downstream had early useful findings; upstream then changed → retain findings; defer re-review
    until prerequisites stabilize.

### Implementation freeze

27. Plan is `In progress` → review-epoch continuation must not rewrite the frozen contract.
