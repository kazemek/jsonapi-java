---
name: implementation-planning
description: Creates, refines, or decomposes research-backed implementation plans under `.agentWork/plans/`, then verifies each with an implementation design review and an implementation plan review executed by fresh-context subagents and bounded fix loops. Use only when the user explicitly requests implementation planning, refinement, or breakdown.
disable-model-invocation: true
---

# Implementation Planning

Produce implementation-ready plan files as temporary execution contracts under
`.agentWork/plans/`, then verify them with a context-isolated design review and a
context-isolated plan review. These files are disposable: they exist only while the work needs a
reviewed contract. Durable knowledge belongs in its canonical owner: current engineering truth in
Snapshot surfaces, stable product direction and principles in Vision, and architectural rationale,
compliance state, public/module contracts, and workflow rules in their respective canonical owners.
Do not create `.agentWork/plans/README.md` or any other plans index. Planning ends only after each
created or refined plan receives both an `implementation-design-review` Pass and an
`implementation-plan-review` Pass, and any superseded original has been deleted. Do not implement
the planned feature.

Design review and plan review must never see this session's context or reasoning. Design reviewers
are fresh subagents that derive everything from the plan contract and repository evidence.
Plan review is a separate fresh subagent after design Pass. Do not re-score reviewer findings.

## Resolve the operation

Determine whether the request is:

- **Create:** no existing live plan covers the requested outcome.
- **Refine:** one not-started plan exists but needs clearer evidence, boundaries, tests, or acceptance criteria.
- **Decompose:** the requested or existing scope cannot reliably fit one implementation and
  review context, or another genuine execution/review boundary applies (see Choose an execution
  unit). Do not decompose merely because a conceptual map has several parts or because numeric
  heuristics are exceeded.

Resolve the target plan unambiguously. Ask the user only when naming, ordering, or scope has
materially different valid choices that repository evidence cannot resolve.

Normal work discovery uses Linear when available. Without Linear, operate only on an explicitly
selected or already-materialized repository plan, or on work the user explicitly supplies; report
the coordination item as unsynchronized rather than inventing hidden backlog state. Never infer the
next project task from `.agentWork/plans/`, Outlook, source layout, or a reconstructed repository
backlog. Do not recreate a permanent repo-side backlog or index as a fallback for Linear.

## Explore before writing

Snapshot-first is an authority rule: completed work, Git archaeology, Linear, and Vision must never
substitute for current-state discovery. It is not a requirement to read every Snapshot file before
selecting work. Read these sources as relevant:

1. `AGENTS.md` and, when refining or decomposing, the target plan under `.agentWork/plans/`.
2. When Linear is available, the selected work item for coordination context only — not as
   engineering truth.
3. Relevant current Snapshot evidence: `settings.gradle.kts` when build membership matters;
   the affected module README; relevant `package-info.java` and Javadoc; implicated accepted
   ADRs and `docs/conformance.md`; narrow current source and tests needed for feasibility or
   constraints.
4. `docs/vision.md` only when the work adds or changes modules, crosses public product
   boundaries, changes stable product direction or principles, or exposes a potential Vision
   conflict.
5. Relevant Outlook only when planning unbuilt or revisable future direction. Outlook is
   planning input: it never overrides Snapshot, Vision, or accepted ADRs, and it never
   satisfies dependencies.
6. Adjacent live plans under `.agentWork/plans/` only when their contracts constrain ordering,
   compatibility, or scope. Open only the files that are implicated; do not treat the directory
   as a backlog to mine for the next task.

Do not scan the whole repository first. Search for overlapping live plans, existing APIs, naming
conventions, diagnostics, fixtures, and test patterns before proposing new concepts. Do not search
historical or deleted plans for current engineering truth.

Do not treat Linear as engineering truth. An optional work-item identifier on a plan is
traceability metadata only. Do not copy Linear ticket prose into Research and constraints.
Do not put work-item identifiers in filenames or paths. Linear is never required to understand
current engineering truth or to implement or review an explicitly selected, already-materialized
repository plan. No Snapshot, planning, implementation, or review gate requires live Linear.

## Research the contract

Research enough to replace assumptions with implementable constraints:

- Prefer official specifications, primary project documentation, standards, and source code.
- Use current external research only when repository evidence is insufficient or the plan depends
  on third-party behavior.
- Record only sources and conclusions that constrain scope, behavior, compatibility, or testing.
- Distinguish confirmed requirements from proposed policy.
- Never copy large source passages into a plan; link to the source and state its implementation
  consequence.

If research exposes a vision conflict, flag it before writing the implementation contract. If the
divergence is intentional, include the required vision update in scope or make it a prerequisite.
Identify consequential, hard-to-reverse decisions that require a new or updated ADR; do not silently
settle them in plan prose. Do not treat Outlook as resolving a vision, ADR, or Snapshot conflict.

## Respect plan lifecycle

Repository execution states are only `Not started` and `In progress`. There is no `Complete`
status. Successful implementation deletes the plan after finalization; do not write `Complete`.

- A `Not started` plan may be refined in place, or replaced by smaller implementation plans when a
  genuine execution/review boundary requires decomposition.
- After replacement plans are created and each has a design-review Pass and a plan-review Pass,
  reconcile incoming references from other live plans **before** deleting the superseded original
  (see Decompose oversized work in [reference.md](reference.md)). Updating a `Not started`
  dependent is a refine of that plan and must use the existing mandatory design-review →
  plan-review pipeline (bounded loops) — not a new review stage. Never silently rewrite an
  `In progress` dependent; if deletion would require that, stop and keep the original. Do not
  retain the superseded original as an umbrella or index. Broader portfolio/grouping belongs in
  Linear; tentative future direction belongs in Outlook.
- Once implementation has started (`In progress`), treat the plan as a fixed delivery contract.
  Status, commits, code changes, or user context may establish that implementation started.
- Never rewrite an implementation-started plan to describe new work. Create a follow-up plan with
  an explicit dependency instead.
- If lifecycle state is uncertain and changes whether a file may be edited, ask the user.

Checked acceptance criteria are delivery claims, not planning evidence. Do not mark criteria
complete while planning.

## Choose an execution unit

Prefer the **largest coherent execution unit that can still be reliably implemented and
independently reviewed in one context**.

Conceptual decomposition may contain many steps or capability areas without creating multiple
execution plans. Capability maps, adapter layers, or named sub-parts (for example Spring
transport vs DTO vs PATCH vs WebFlux, or a Jackson 2 parity track) may be recorded in Outlook or
Linear as direction or backlog. They are not by themselves a reason to emit N plan files.

An implementable execution plan should still be coherent: one outcome that can be implemented
and reviewed in one context, normally one principal capability, with independent value at
completion. Numeric bounds of at most five deliverables and eight acceptance criteria (including
completion gates) are **heuristics** for that one-context check, not an automatic decomposition
rule. Markdown length is not a size measure. Cross-module work is allowed when the integration
itself is one coherent change.

Create separate execution plans only when there is a genuine execution/review boundary:

- the work cannot reliably fit one implementation/review context;
- independently landable capabilities do not need to be atomic;
- materially different prerequisites block parts independently;
- distinct hard-to-reverse architectural decisions require separate review;
- separate modules or workstreams genuinely should not land as one coherent change.

When uncertain, keep one plan unless a boundary above is clear. For decompose steps, the full
file template, and nullness / `module-docs` hooks, see [reference.md](reference.md).

## Write the plan files

Create or update files under `.agentWork/plans/`. Use descriptive filenames and titles; do not use
phase numbers or work-item identifiers as structural identity. Required sections: Goal, Research
and constraints, Deliverables, Non-goals, Implementation boundaries, Test strategy, Acceptance
criteria (plus Module/Scope, Dependencies, Status metadata, and optional Work item). Use the
template in [reference.md](reference.md).

## Verify

After writing all plan files:

1. Confirm every `Dependencies` header is hard execution-order prerequisites: relative Markdown
   links to surviving live plan files, or `None`. Never Linear IDs, Outlook, deleted plans, bare
   titles, or bare path stems alone. Parallel-safe work must have no dependency edge between those
   plans.
2. Confirm each plan has a Work item identifier or an explicit unsynchronized-coordination note.
3. Confirm filenames, titles, and dependencies use descriptive plan identity — no phase-number
   identity.
4. Reapply the execution-unit rule to each emitted plan.
5. Confirm decomposed plans do not overlap or omit requirements from the source request.
6. Confirm plan prose links to rather than duplicates applicable canonical sources (vision, ADR,
   conformance, Outlook, and module documentation). Omit Outlook unless the work is unbuilt or
   revisable future direction.
7. Do not create `.agentWork/plans/README.md` or otherwise index the plans directory as a backlog.

Then proceed to design review, then plan review. Do not treat planning as finished until every
created or refined plan in this run has both a design-review Pass and a plan-review Pass (or the
applicable fix-loop cap / Blocked stop is reached). After a decompose replacement set has both
Passes, run the incoming-reference reconciliation and dependent re-review sequence in
[reference.md](reference.md) before deleting the superseded original.

## Review design with fresh context

The design review is mandatory and non-negotiable. Its purpose is to verify that the technical
design is sound, without any influence from this session's context or reasoning.

1. Collect the list of plan files created or refined in this run. Review each one. A superseded
   original being replaced is not itself a review target; the replacement plans are.
2. For each such plan, follow the **Orchestration** section of
   `.agents/skills/implementation-design-review/SKILL.md` exactly. Do not duplicate spawn, prompt,
   combination, or stub text here.
3. Handle the official verdict from the pointer stub. Trust that string; do not re-score findings:
   - **Pass:** proceed to plan review for that plan.
   - **Changes required:** fix the findings in the affected plan file(s), re-run Verify, then
     re-run Orchestration with NEW fresh subagents. Cap the loop at two re-reviews; when the
     official verdict is still `Changes required`, stop and report the remaining findings. Do not
     send prior-review summaries to the new subagents. Replace artifacts rather than append.
   - **Blocked:** stop and report. Do not run plan-review.
4. An in-place edit of a file already under review does not reset that file's remaining
   design-review cap. Renaming it does not start a new cap. If a design-review `Changes required`
   fix splits into new plan files, add those files to this run's design-review list; each is a
   new contract with its own cap (same as files created before the first review). If that loop has
   already stopped, or design review has already Passed, do not split as a continuation of this run;
   a split then is a new `implementation-planning` invocation.

## Review plan with fresh context

The plan review is mandatory after design Pass and is non-negotiable. Its purpose is to verify each
plan contract without any influence from this session's context or reasoning. Do not start
plan-review for a plan until its design-review official verdict is Pass.

1. For each plan that has a design-review Pass, spawn a NEW general-purpose subagent with write
   access (for example, opencode `general` or the equivalent general subagent in the harness in use):
   - fresh context: never resume or reuse a previous subagent session;
   - write capability: it must create the review artifact under `.agentWork/.session/`.
2. Send the reviewer prompt below verbatim, filling only the placeholder. Do not add anything to
   it: no summaries, self-assessment, reasoning, planning narrative, or draft diffs.
3. Never answer the reviewer's questions with planning narrative. When it asks for facts, direct it
   to repository evidence (files, the plan contract).
4. When the harness cannot spawn a write-capable fresh subagent, fall back to a manual fresh
   session: follow `.agents/skills/implementation-handoff/SKILL.md` with the plan path and suggested
   skill `implementation-plan-review`, then print the one-liner it produces.

Plan-review edits in this run must not restart design review. If the approach later changes, a new
`implementation-planning` refine runs design review again.

### Reviewer prompt (send verbatim)

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

## Handle the plan-review verdict

Overall planning Pass only when every created or refined plan in this run receives both a
design-review Pass and a plan-review Pass.

- **Pass (all remaining plan reviews):** if this run replaced a `Not started` original with smaller
  plans, run the decomposition finalization sequence in [reference.md](reference.md)
  (incoming-reference reconciliation → re-review refined `Not started` dependents → mechanical
  verify → delete). On `In progress` dependent blocker, review Blocked, or exhausted fix loop,
  stop and keep the original. Report and finish.
- **Changes required (any):** fix the findings in the affected plan file(s), re-run Verify, then
  re-review each affected plan with a NEW fresh plan-review subagent. Cap the loop at two
  re-reviews; when any verdict is still `Changes required`, stop and report the remaining findings.
  Do not restart design review. Do not delete a superseded original until every replacement has
  Passed and decomposition finalization succeeds.
- **Blocked (any):** stop and report. Do not delete a superseded original.

## Report

Report to the user: the files created or refined (and any superseded original deleted), research
that materially changed the scope, any decomposition rationale, the design-review stub path(s) and
official verdict(s), the plan-review artifact path(s) and verdict(s), Linear sync or explicit
unsync, and residual risks. When this run decomposes into N plans, expect N design-review stubs and
N plan-review artifacts (and re-reviews for any that need changes).
