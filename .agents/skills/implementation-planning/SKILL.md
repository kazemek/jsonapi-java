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

## Resolve the operation

- **Create:** no live plan covers the requested outcome.
- **Refine:** one `Not started` plan needs clearer evidence, boundaries, tests, or acceptance
  criteria.
- **Decompose:** a `Not started` plan or requested scope has a genuine execution/review boundary.
  Conceptual parts or exceeded numeric heuristics alone are insufficient.

Resolve all affected plans unambiguously; ask only when evidence cannot resolve materially
different naming, ordering, or scope choices. An existing plan may be refined or decomposed only
before implementation starts. Status, commits, code changes, or user context may establish that
implementation has started even when status is stale. Treat an implementation-started plan as a
fixed delivery contract; create a dependent follow-up instead of expanding it. Ask when state is
uncertain, and never mark planning acceptance criteria complete.

Use the external work tracker for normal work discovery when available, but only as optional
coordination metadata. Without it, act only on a user-supplied outcome or explicitly
selected/materialized plan and report coordination as unsynchronized. Never infer backlog order from
plans, Outlook, source layout, Git history, or a reconstructed backlog; never put work-item IDs in
plan identity.

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

Both reviews are mandatory. Review each created/refined plan; for decomposition, review replacements
rather than the superseded original. Reviewers derive facts only from the contract and repository evidence. Never
send this session's summaries, reasoning, narrative, self-assessment, or diffs; never re-score a
reported verdict. Replace artifacts on re-review.

### Design review

Follow **Orchestration** in
`.agents/skills/implementation-design-review/SKILL.md` exactly, including its parallel fresh,
write-capable reviewers, verbatim isolated prompts, worst-wins result, pointer stub, and terminating
`implementation-handoff` fallback. Handle only the official pointer-stub verdict:

- `Pass`: proceed to plan review.
- `Changes required`: fix the plan, repeat **Write and verify**, then rerun Orchestration with new
  fresh reviewers. Allow at most two re-reviews per plan; after the second, stop and report any
  remaining findings.
- `Blocked`: stop and do not run plan review.

Editing or renaming a reviewed file does not reset its cap. A pre-Pass fix that splits the contract
creates new plans, each with its own cap. After the loop stops or design has Passed, splitting
requires a new `implementation-planning` invocation.

### Plan review

Run only after design `Pass`. Derive `<plan basename>` from the filename without `.md`. Spawn a new
general-purpose, write-capable subagent in a fresh session and send this prompt verbatim, replacing
only `<plan path>` and `<plan basename>`:

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

- `Pass`: the plan is accepted. Plan-review fixes never restart design review; an approach change
  instead requires a new planning refinement and design review.
- `Changes required`: fix, repeat **Write and verify**, and re-review with a new fresh subagent.
  Allow at most two plan re-reviews per plan; after the second, stop and report remaining findings.
- `Blocked`: stop and report.

Overall Pass requires both review Passes for every plan. After replacement plans Pass, execute
[decomposition finalization](reference.md#decompose-oversized-work), including the same bounded
fresh design/plan review loops for modified `Not started` dependents. On an `In progress` dependent,
Blocked review, or exhausted loop, retain the superseded original.

## Report

Report created/refined/deleted paths, material research or decomposition decisions, each design
stub and official verdict, each plan-review artifact and verdict, coordination sync or explicit
unsync, and residual risks.
