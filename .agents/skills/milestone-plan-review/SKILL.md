---
name: milestone-plan-review
description: Reviews a milestone implementation plan (spec) against planning rules and repository evidence, and writes the result to `.agentWork/.session/`. Use when the user explicitly requests a milestone plan review, spec audit, or planning acceptance review, or when `milestone-planning` delegates review to a fresh-context subagent.
disable-model-invocation: true
---

# Milestone Plan Review

Review one milestone contract as a planning artifact. Do not implement the planned feature, run
feature completion builds, or modify milestones, index, vision, ADRs, or sources unless the user
separately asks for changes. The `milestone-planning` skill owns fixes in its review loop.

## Resolve the review inputs

1. Identify exactly one target file under `.agentWork/milestones/`.
   - Use a path, phase, or milestone name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple milestones are plausible, ask the user to choose.
2. Read:
   - the target milestone;
   - `AGENTS.md`;
   - `.agentWork/milestones/README.md`;
   - `.agents/skills/milestone-planning/SKILL.md` for the planning contract to enforce;
   - affected module READMEs and package documentation when the milestone names them;
   - `docs/vision.md` when the milestone changes project direction, modules, or public product
     boundaries, or when the spec suggests a vision conflict;
   - ADRs and conformance sections linked by the milestone or affected module documentation, plus
     additional records only when directly implicated by the planned scope.
3. Inspect narrow production and test files only to check feasibility claims in the milestone — not
   to score an implementation.
4. State the reviewed contract boundary in the artifact (milestone path and any index lines
   inspected). Never imply that unexamined milestones or code were reviewed.

## Perform the review

1. Map the milestone goal, deliverables, non-goals, dependencies, implementation boundaries, test
   strategy, and each acceptance criterion to planning-quality evidence.
2. Look first for:
   - size-gate violations: more than one coherent outcome, more than five deliverables, more than
     eight acceptance criteria (including applicable repository completion gates), or scope that
     cannot fit one focused coding-agent task and reviewable commit;
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
   - missing `module-docs` deliverable and checklist acceptance criterion when the milestone adds a
     submodule or changes public packages, entry points, validate/read flows, non-goals, or
     agent-relevant invariants — reference the skill; do not copy its checklist into the review;
   - lifecycle violations: rewriting a completed or implementation-started milestone instead of a
     follow-up; checked acceptance criteria used as planning evidence;
   - index drift: dependency order or `milestone — module/scope — status` entry missing or
     mismatched against the milestone file;
   - duplication of vision, ADR, conformance, or module-docs checklist prose instead of links.
3. Assign each actionable finding a severity:
   - **Critical:** fundamentally invalidates the plan or makes it unsafe to implement as written.
   - **High:** major planning-contract violation (size gate, lifecycle, missing gates, vision/ADR
     conflict) that would derail implementation or review.
   - **Medium:** real clarity, testability, or boundary gap with bounded impact.
   - **Low:** minor issue worth fixing; omit optional style preferences.
4. Give every finding:
   - a concise title;
   - file and line evidence (milestone sections/lines; index lines when relevant);
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
.agentWork/.session/milestone-plan-review-<milestone-basename>.md
```

For `.agentWork/milestones/phase-1-1-spec-data-model.md`, write
`.agentWork/.session/milestone-plan-review-phase-1-1-spec-data-model.md`.

Use this template:

```markdown
# Milestone Plan Review: <milestone title>

> **Milestone:** `<milestone path>`
> **Review scope:** <milestone path and inspected index/module/docs paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Planning requirement:** <size gate, section, acceptance criterion, or index rule>
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No actionable findings." when none exist.>

## Contract coverage

- [Pass | Fail | Partial | Not verified] Goal
  - Evidence: <paths, lines, or explanation>
- [Pass | Fail | Partial | Not verified] Size gate
  - Evidence: <deliverable/AC counts and outcome coherence>
- [Pass | Fail | Partial | Not verified] Research and constraints
  - Evidence: <sources and implementation consequences>
- [Pass | Fail | Partial | Not verified] Deliverables
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Non-goals
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Implementation boundaries
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Test strategy
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Acceptance criteria
  - Evidence: <…>
- [Pass | Fail | Partial | Not verified] Index sync
  - Evidence: <dependency order and index entry>
- [Pass | Fail | Partial | Not verified] Lifecycle
  - Evidence: <status and editability>
- [Pass | Fail | Partial | Not applicable] Nullness / `module-docs` hooks
  - Evidence: <why required or why not applicable>

## Repo evidence inspected

- `<path>` — <why inspected>

## Residual risks

<Unverified feasibility, unavailable evidence, dependency concerns, or "None identified.">
```

The artifact is ephemeral and non-canonical. On every re-review, replace the prior artifact for that
milestone instead of appending history. After writing it, report the artifact path and verdict to
the user.
