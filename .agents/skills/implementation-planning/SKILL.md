---
name: implementation-planning
description: Creates, refines, or decomposes research-backed implementation plans under `.agentWork/plans/`, then verifies each with an implementation design review and an implementation plan review executed by fresh-context subagents and bounded fix loops. Use only when the user explicitly requests implementation planning, refinement, or breakdown.
disable-model-invocation: true
---

# Implementation Planning

Create temporary, implementation-ready contracts under `.agentWork/plans/`. `AGENTS.md` owns
repository authority, lifecycle, canonical knowledge, and completion-gate classification; apply it
rather than restating it here. Never implement the planned work or create a plans index. Planning
finishes only when every created/refined plan passes fresh design and plan reviews and any
superseded plan is safely reconciled and deleted.

Shared finding severity and stage ownership:
[../review-findings.md](../review-findings.md). Epoch ledgers, budgets, dependency waves, and
scenario expectations: [reference.md](reference.md).

## Resolve the operation

- **Create:** no live plan covers the requested outcome.
- **Refine:** one `Not started` plan needs clearer evidence, boundaries, tests, or acceptance
  criteria. When prior design/plan epoch status is `passed`, this explicit Refine opens new bounded
  review epochs per [review epochs](reference.md#review-epochs) while preserving history.
- **Decompose:** a `Not started` plan or requested scope has a genuine execution/review boundary.
  Conceptual parts or exceeded numeric heuristics alone are insufficient.

Resolve all affected plans unambiguously; ask only when evidence cannot resolve materially
different naming, ordering, or scope choices. Choose the final filename before the first review
ledger exists; after any ledger exists for a plan, do not rename it. An existing plan may be refined
or decomposed only before implementation starts. Status, commits, code changes, or user context may
establish that implementation has started even when status is stale. Treat an implementation-started
plan as a fixed delivery contract; create a dependent follow-up instead of expanding it. Ask when
state is uncertain, and never mark planning acceptance criteria complete.

Use the external work tracker for normal work discovery when available, but only as optional
coordination metadata. Without it, act only on a user-supplied outcome or explicitly
selected/materialized plan and report coordination as unsynchronized. Never infer backlog order from
plans, Outlook, source layout, Git history, or a reconstructed backlog; never put work-item IDs in
plan identity. When a plan is materialized with a linked external work item, synchronize the item's
plan reference when the tracker is configured/available; otherwise report coordination as
unsynchronized.

## Build the contract

Start with current Snapshot evidence. Read `AGENTS.md`, the target plan when applicable, and only
the implicated module README, package documentation/Javadoc, source/tests, accepted ADRs, and
conformance sections. Read `settings.gradle.kts` when membership matters; Vision only for its
`AGENTS.md`-defined boundary; relevant Outlook only for revisable future direction; and adjacent
live plans only when they constrain scope, compatibility, or order. External work-tracker metadata,
Outlook, completed work, and Git archaeology cannot substitute for current-state discovery or
satisfy dependencies.
Do not scan the whole repository or use deleted plans as current truth.

Research externally only when repository evidence is insufficient or third-party behavior matters.
Record concise authoritative sources and implementation consequences, distinguish requirements
from proposed policy, surface Snapshot/Vision/ADR conflicts, and require an ADR for consequential
hard-to-reverse decisions rather than settling them in plan prose. If a Vision divergence is
intentional, include the required Vision update in plan scope or make it a prerequisite.

Before proposing new concepts, search narrowly for overlapping live plans, existing APIs, naming
conventions, diagnostics, fixtures, and test patterns in the affected scope.

## Choose an execution unit

Prefer the largest coherent outcome that can be reliably implemented and independently reviewed in
one context, normally one principal capability with value at completion. Cross-module integration
may be one unit. Five deliverables and eight acceptance criteria (including gates) are heuristics,
not split rules; Markdown length and conceptual sub-parts are not size measures.

Split only when work cannot fit one implementation/review context, independent capabilities need
not land atomically, prerequisites block parts differently, architectural decisions need separate
review, or modules/workstreams should genuinely land separately. Otherwise keep one plan. Follow
[Decompose oversized work](reference.md#decompose-oversized-work) for replacement and
reconciliation behavior.

## Write and verify

Use descriptive filenames/titles and the [plan template](reference.md#plan-file-template); never
use phase numbers or work-item IDs as structural identity. Before review, verify:

1. Each `Dependencies` value is `None` or relative Markdown links to surviving live plans that are
   true hard execution-order prerequisites. Parallel-safe plans have no edge; external work-item
   IDs, Outlook, deleted plans, titles, and bare stems are invalid.
2. Each plan has a work-item ID or an explicit coordination-unsynchronized note.
3. Every plan remains one coherent execution unit; decomposition neither overlaps nor omits the
   source requirements.
4. Plan prose links to canonical owners instead of duplicating them and includes only applicable
   `AGENTS.md` completion gates. Omit Outlook unless the work concerns unbuilt or revisable future
   direction.
5. No `.agentWork/plans/README.md` or equivalent backlog/index exists.

## Review with fresh context

Both reviews are mandatory. Review each created/refined plan in
[dependency-aware waves](reference.md#dependency-aware-review-waves); for decomposition, review
replacements rather than the superseded original. Reviewers derive facts only from the contract and
repository evidence. Never send this session's summaries, reasoning, narrative, self-assessment, or
diffs; never re-score a reported verdict. Manage [review epochs](reference.md#review-epochs),
archive prior fixed-path artifacts before each new attempt, and maintain design/plan gate
carry-forward after every attempt and on exhaust.

Do not add a separate mandatory plan-set architecture review. Shared architecture belongs in the
earliest prerequisite plan; wave-ordered design review of that foundation covers the set without a
third review layer.

### Design review

Follow **Orchestration** in
`.agents/skills/implementation-design-review/SKILL.md` exactly, including its parallel fresh,
write-capable reviewers, verbatim isolated prompts, worst-wins result, pointer stub, and terminating
`implementation-handoff` fallback. After each attempt (and on exhaust), union unresolved design gate
findings into the design gate carry-forward per [review epochs](reference.md#review-epochs). Handle
only the official pointer-stub verdict:

- `Pass`: proceed to plan review only when no unresolved design **Blocking** remain in the design
  gate carry-forward. Unresolved design `Required` findings remain in carry-forward for plan review;
  they must not trigger another design cycle by themselves.
- `Changes required`: fix **all known** `Blocking` findings (including design gate carry-forward
  Blocking), repeat **Write and verify**, then run **one** fresh design re-review in the same epoch.
  If `Blocking` findings remain after that re-review, exhaust the epoch, update gate carry-forward,
  stop, and return control to the user.
- `Blocked`: set the design epoch to `blocked` per [review epochs](reference.md#review-epochs), stop,
  and do not run plan review. Resume later only via blocked-resume of the same epoch.

`Required` and `Advisory` findings alone never start another design-review cycle. Editing the plan,
renaming after a ledger exists, or restarting a session does not reset epoch budgets. Fake
split/replacement solely to obtain new counters is forbidden; genuine decomposition remains valid
only under **Choose an execution unit**. After the loop stops or design has Passed, splitting
requires a new `implementation-planning` invocation. Review-epoch continuation, blocked-resume, and
Refine re-entry are only for `Not started` plans; never use them to rewrite an `In progress` frozen
contract.

### Plan review

Run only after design `Pass` for that plan (with no unresolved design Blocking in carry-forward),
and only when the plan is in a ready dependency wave.
Derive `<plan basename>` from the filename without `.md`. Spawn a new general-purpose, write-capable
subagent in a fresh session and send this prompt verbatim, replacing only `<plan path>` and
`<plan basename>`:

```text
You are the implementation plan reviewer for this repository. Your context was intentionally
started empty so you review independently of the planning session.

Task inputs (the only facts you may assume):
- Plan: <plan path>
- Review artifact: .agentWork/.session/implementation-plan-review-<plan basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/implementation-plan-review/SKILL.md and follow it exactly.
2. Base every conclusion only on the plan contract and repository evidence. Do not accept or
   ask for summaries from the planning session; ignore editor or IDE state.
3. Write the artifact, then report the artifact path and verdict.
```

Direct factual questions to repository evidence, not planning narrative. If a fresh write-capable
subagent cannot run, follow `.agents/skills/implementation-handoff/SKILL.md` for the plan and
`implementation-plan-review`, print its one-liner, and stop for the fresh-session result.

- `Pass`: the plan is accepted for planning purposes only when plan gate carry-forward has no
  unresolved Blocking/Required. Clear carry-forward entries that plan review verified as addressed.
- `Changes required`: fix **all known** `Blocking`/`Required` findings (including plan and design
  gate carry-forward), repeat **Write and verify**, and run **one** fresh plan re-review in the same
  epoch. If findings remain after that re-review, exhaust the epoch, update plan gate carry-forward,
  stop, and return control to the user. Ordinary completeness fixes do not restart design review. A
  `Blocking` architectural finding uses the architectural-escalation transition in
  [review epochs](reference.md#review-epochs) (one automatic new design epoch when design is
  `passed`; otherwise stop for authorization) instead of endless plan-only patches.
- `Blocked`: set the plan epoch to `blocked` per [review epochs](reference.md#review-epochs), stop,
  and report. Resume later only via blocked-resume of the same epoch.

Overall Pass requires both review Passes for every plan in this operation. After replacement plans
Pass, execute [decomposition finalization](reference.md#decompose-oversized-work), including
wave-ordered bounded fresh design/plan review loops for modified `Not started` dependents. On an
`In progress` dependent, Blocked review, or exhausted epoch, retain the superseded original.

## Report

Report created/refined/deleted paths, material research or decomposition decisions, each design
stub and official verdict, each plan-review artifact and verdict, epoch ledger paths and any
blocked-resume / exhausted-continuation / Refine / architectural-escalation authorization,
coordination sync or explicit unsync, and residual risks.
