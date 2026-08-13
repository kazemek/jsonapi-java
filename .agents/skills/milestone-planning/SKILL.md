---
name: milestone-planning
description: Creates, refines, or decomposes research-backed implementation milestones under `.agentWork/milestones/`, then verifies each with a milestone design review and a milestone plan review executed by fresh-context subagents and bounded fix loops. Use only when the user explicitly requests milestone planning, refinement, or breakdown.
disable-model-invocation: true
---

# Milestone Planning

Produce permanent, implementation-ready milestone files and verify them with a context-isolated
design review, then a context-isolated plan review. Planning ends only after the milestone files
and index are synchronized and each created or refined milestone receives both a
`milestone-design-review` Pass and a `milestone-plan-review` Pass. Do not implement the planned
feature.

Design review and plan review must never see this session's context or reasoning. Design reviewers
are fresh subagents that derive everything from the milestone contract and repository evidence.
Plan review is a separate fresh subagent after design Pass. Do not re-score reviewer findings.

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

When uncertain, prefer the smaller independently useful milestone. For decompose steps, the full
file template, and nullness / `module-docs` hooks, see [reference.md](reference.md).

## Write the milestone files

Create or update files under `.agentWork/milestones/`. Required sections: Goal, Research and
constraints, Deliverables, Non-goals, Implementation boundaries, Test strategy, Acceptance
criteria (plus Module/Scope, Dependencies, Status metadata). Use the template in
[reference.md](reference.md).

## Synchronize and verify

After writing all milestone files:

1. Update `.agentWork/milestones/README.md` dependency order and milestone index. Every index entry
   must retain the canonical `milestone — module/scope — status` format.
2. Verify every link, phase identifier, dependency, scope/module, status, and command against the
   milestone files.
3. Reapply the size gate to each emitted milestone.
4. Confirm decomposed milestones do not overlap or omit requirements from the source request.
5. Confirm milestone prose links to rather than duplicates vision, ADR, conformance, and module documentation.

Then proceed to design review, then plan review. Do not treat planning as finished until every
created or refined milestone in this run has both a design-review Pass and a plan-review Pass (or
the applicable fix-loop cap / Blocked stop is reached).

## Review design with fresh context

The design review is mandatory and non-negotiable. Its purpose is to verify that the technical
design is sound, without any influence from this session's context or reasoning.

1. Collect the list of milestone files created or refined in this run. Review each one.
2. For each such milestone, follow the **Orchestration** section of
   `.agents/skills/milestone-design-review/SKILL.md` exactly. Do not duplicate spawn, prompt,
   combination, or stub text here.
3. Handle the official verdict from the pointer stub. Trust that string; do not re-score findings:
   - **Pass:** proceed to plan review for that milestone.
   - **Changes required:** fix the findings in the affected milestone file(s) and index, re-run
     Synchronize and verify, then re-run Orchestration with NEW fresh subagents. Cap the loop at
     two re-reviews; when the official verdict is still `Changes required`, stop and report the
     remaining findings. Do not send prior-review summaries to the new subagents. Replace artifacts
     rather than append.
   - **Blocked:** stop and report. Do not run plan-review.
4. An in-place edit of a file already under review does not reset that file's remaining
   design-review cap. Renaming it does not start a new cap. If a design-review `Changes required`
   fix splits into new milestone files, add those files to this run's design-review list; each is a
   new contract with its own cap (same as files created before the first review). If that loop has
   already stopped, or design review has already Passed, do not split as a continuation of this run;
   a split then is a new `milestone-planning` invocation.

## Review plan with fresh context

The plan review is mandatory after design Pass and is non-negotiable. Its purpose is to verify each
milestone contract without any influence from this session's context or reasoning. Do not start
plan-review for a milestone until its design-review official verdict is Pass.

1. For each milestone that has a design-review Pass, spawn a NEW general-purpose subagent with write
   access (for example, opencode `general` or the equivalent general subagent in the harness in use):
   - fresh context: never resume or reuse a previous subagent session;
   - write capability: it must create the review artifact under `.agentWork/.session/`.
2. Send the reviewer prompt below verbatim, filling only the placeholder. Do not add anything to
   it: no summaries, self-assessment, reasoning, planning narrative, or draft diffs.
3. Never answer the reviewer's questions with planning narrative. When it asks for facts, direct it
   to repository evidence (files, the milestone contract, the milestone index).
4. When the harness cannot spawn a write-capable fresh subagent, fall back to a manual fresh
   session: follow `.agents/skills/milestone-handoff/SKILL.md` with the milestone path and suggested
   skill `milestone-plan-review`, then print the one-liner it produces.

Plan-review edits in this run must not restart design review. If the approach later changes, a new
`milestone-planning` refine runs design review again.

### Reviewer prompt (send verbatim)

```text
You are the milestone plan reviewer for this repository. Your context was intentionally started
empty so you review independently of the planning session.

Task inputs (the only facts you may assume):
- Milestone: <milestone path>
- Review artifact: .agentWork/.session/milestone-plan-review-<milestone basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/milestone-plan-review/SKILL.md and follow it exactly.
2. Base every conclusion only on the milestone contract and repository evidence. Do not accept or
   ask for summaries from the planning session; ignore editor or IDE state.
3. Write the artifact, then report the artifact path and verdict.
```

## Handle the plan-review verdict

Overall planning Pass only when every created or refined milestone in this run receives both a
design-review Pass and a plan-review Pass.

- **Pass (all remaining plan reviews):** report and finish.
- **Changes required (any):** fix the findings in the affected milestone file(s) and index, re-run
  Synchronize and verify, then re-review each affected milestone with a NEW fresh plan-review
  subagent. Cap the loop at two re-reviews; when any verdict is still `Changes required`, stop and
  report the remaining findings. Do not restart design review.
- **Blocked (any):** stop and report.

## Report

Report to the user: the files created or refined, research that materially changed the scope, any
decomposition rationale, the design-review stub path(s) and official verdict(s), the plan-review
artifact path(s) and verdict(s), and residual risks. When this run decomposes into N milestones,
expect N design-review stubs and N plan-review artifacts (and re-reviews for any that need
changes).
