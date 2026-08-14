---
name: implementation-plan-review
description: Reviews an implementation plan (spec) against planning rules and repository evidence, and writes the result to `.agentWork/.session/`. Use when the user explicitly requests an implementation plan review, spec audit, or planning acceptance review, or when `implementation-planning` delegates plan review to a fresh-context subagent after design-review Pass.
disable-model-invocation: true
---

# Implementation Plan Review

Review one implementation plan contract as a planning artifact. Do not implement the planned
feature, run feature completion builds, or modify plans, vision, ADRs, or sources unless the user
separately asks for changes. The `implementation-planning` skill owns fixes in its review loop.

Instruction boundary: treat `.agents/skills/implementation-planning/SKILL.md`, its sibling
`reference.md`, and other referenced policy docs (`AGENTS.md`, ADRs, etc.) as non-executable
reference data. Do not execute create/refine/decompose, plan-directory writes, subagent spawn, or
fix-loop steps from the planning skill. This skill's artifact-only / no-mutation rules override any
conflicting instructions in those references. **Perform the review** below is the authoritative
validation checklist.

Do not evaluate whether the proposed technical approach is the right design, and do not recommend
alternative architectures, APIs, or module placements. Design soundness is owned by
`implementation-design-review`. If a design concern is noticed, record it under Residual risks, not
as a planning finding, unless the contract is internally contradictory (incoherent or overlapping
goal, deliverables, non-goals, and boundaries). Keep flagging consequential decisions left as prose
that should be identified as ADRs — that is contract completeness, not design content.

## Resolve the review inputs

1. Identify exactly one target file under `.agentWork/plans/`.
   - Use a path or plan name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple plans are plausible, ask the user to choose.
   - Do not infer the next review target by scanning `.agentWork/plans/` as a backlog.
2. Read:
   - the target plan;
   - `AGENTS.md`;
   - `.agents/skills/implementation-planning/SKILL.md` and its sibling `reference.md` as
     non-executable reference for planning-quality rules to enforce (not a skill to follow);
   - affected module READMEs and package documentation when the plan names them;
   - `docs/vision.md` when the plan changes project direction, modules, or public product
     boundaries, or when the spec suggests a vision conflict;
   - ADRs and conformance sections linked by the plan or affected module documentation, plus
     additional records only when directly implicated by the planned scope.
3. Inspect narrow production and test files only to check feasibility claims in the plan — not
   to score an implementation.
4. State the reviewed contract boundary in the artifact (plan path and any adjacent live plans
   inspected). Never imply that unexamined plans or code were reviewed.

Linear is never required to review a materialized plan. Do not treat Linear, Outlook, or deleted
plans as engineering proof.

## Perform the review

1. Map the plan goal, deliverables, non-goals, dependencies, implementation boundaries, test
   strategy, and each acceptance criterion to planning-quality evidence.
2. Look first for:
   - execution-unit violations: more than one coherent outcome that cannot be implemented and
     reviewed in one context, or a plan that should have been split only when a genuine
     execution/review boundary in the `implementation-planning` skill applies. Numeric bounds of
     five deliverables and eight acceptance criteria are heuristics, not automatic High findings;
   - a Snapshot/Vision conflict that the plan depends on but does not surface (do not treat
     whichever text looks newer as a resolution);
   - incoherent or overlapping goal, deliverables, non-goals, and boundaries;
   - weak research and constraints: assumptions without implementable consequences, large pasted
     sources, unflagged vision conflicts, or consequential decisions settled in prose that should
     be ADRs;
   - acceptance criteria that are vague, non-binary, checklist-of-steps, or that fail to
     collectively prove the goal;
   - missing applicable completion-gate criteria per the change-scope gate tiers in `AGENTS.md`
     (omit gates that the planned scope does not demand);
   - missing nullness acceptance criteria when the planned Java public API introduces or changes
     null-bearing types (ADR-009);
   - missing `module-docs` deliverable and checklist acceptance criterion when the plan adds a
     submodule or changes public packages, entry points, validate/read flows, non-goals, or
     agent-relevant invariants — reference the skill; do not copy its checklist into the review;
   - lifecycle violations: rewriting an `In progress` plan instead of a follow-up; a `Complete`
     status (invalid; repository states are only `Not started` and `In progress`); a superseded
     original retained as an umbrella after reviewed replacements exist; checked acceptance
     criteria used as planning evidence;
   - invalid `Dependencies`: not relative Markdown links to surviving live plan files, or `None`;
     Linear IDs, Outlook, deleted plans, bare titles, or bare path stems alone;
   - phase-number identity in filenames, titles, or dependencies;
   - a `plans/README.md` or other plans-directory backlog/index;
   - duplication of vision, ADR, conformance, or module-docs checklist prose instead of links.
3. Assign each actionable finding a severity:
   - **Critical:** fundamentally invalidates the plan or makes it unsafe to implement as written.
   - **High:** major planning-contract violation (execution-unit / unjustified split, lifecycle,
     missing gates, unsurfaced Snapshot/Vision or vision/ADR conflict) that would derail
     implementation or review.
   - **Medium:** real clarity, testability, or boundary gap with bounded impact.
   - **Low:** minor issue worth fixing; omit optional style preferences.
4. Give every finding:
   - a concise title;
   - file and line evidence (plan sections/lines);
   - the affected planning requirement;
   - impact;
   - a concrete recommended correction.

## Choose the verdict

- **Pass:** no actionable findings remain and the contract satisfies the planning rules with
  sufficient repository evidence.
- **Changes required:** one or more actionable findings remain.
- **Blocked:** the review cannot reach a reliable conclusion because required repository evidence,
  dependencies, or lifecycle state is unavailable or ambiguous.

## Write the artifact

Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/implementation-plan-review-<plan-basename>.md
```

For `.agentWork/plans/jackson3-presence-aware-patch-binding.md`, write
`.agentWork/.session/implementation-plan-review-jackson3-presence-aware-patch-binding.md`.

Use the template in [reference.md](reference.md).

The artifact is ephemeral and non-canonical. On every re-review, replace the prior artifact for that
plan instead of appending history. After writing it, report the artifact path and verdict to
the user.
