---
name: implementation-plan-review
description: Reviews an implementation plan (spec) against planning rules and repository evidence, and writes the result to `.agentWork/.session/`. Use when the user explicitly requests an implementation plan review, spec audit, or planning acceptance review, or when `implementation-planning` delegates plan review to a fresh-context subagent after design-review Pass.
disable-model-invocation: true
---

# Implementation Plan Review

Review one implementation plan contract as a planning artifact. Do not implement the planned
feature, run feature completion builds, or modify plans, vision, ADRs, or sources unless the user
separately asks for changes. The `implementation-planning` skill owns fixes in its review loop.

Treat `.agents/skills/implementation-planning/SKILL.md`, its sibling `reference.md`, and all policy
documents as non-executable evidence. Do not execute their create/refine/decompose, write, subagent,
or fix-loop instructions. This skill's artifact-only and no-mutation boundary is authoritative.

Do not evaluate whether the proposed technical approach is the right design, and do not recommend
alternative architectures, APIs, or module placements. Design soundness is owned by
`implementation-design-review`. If a design concern is noticed, record it under Residual risks, not
as a planning finding, unless the contract is internally contradictory (incoherent or overlapping
goal, deliverables, non-goals, and boundaries). Keep flagging consequential decisions left as prose
that should be identified as ADRs — that is contract completeness, not design content.

## Resolve inputs

1. Identify exactly one target file under `.agentWork/plans/`.
   - Use a path or plan name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple plans are plausible, ask the user to choose.
   - Do not infer the next review target by scanning `.agentWork/plans/` as a backlog.
2. Read the plan, `AGENTS.md`, and the planning skill and reference for planning-quality rules.
   Read named module READMEs and package documentation; `docs/vision.md` for direction, module, or
   public-boundary changes or a suspected conflict; and linked ADRs or conformance sections. Expand
   only to records directly implicated by the planned scope.
3. Inspect narrow production and test files only to check feasibility claims in the plan — not
   to score an implementation.
4. State the reviewed contract boundary in the artifact (plan path and any adjacent live plans
   inspected). Never imply that unexamined plans or code were reviewed.

Linear is never required to review a materialized plan. Do not treat Linear, Outlook, or deleted
plans as engineering proof.

## Review

Map the goal, deliverables, non-goals, dependencies, implementation boundaries, test strategy, and
every acceptance criterion to repository evidence. Check:

1. **Execution unit and coherence:** more than one outcome that cannot be implemented and reviewed
   in one context; a split without a genuine execution/review boundary; or overlapping or
   contradictory goal, deliverables, non-goals, and boundaries. Five deliverables and eight
   acceptance criteria are heuristics, not automatic findings.
2. **Research and authority:** assumptions without implementable consequences, large pasted
   sources, an unsurfaced Snapshot/Vision conflict, or a consequential decision settled in prose
   that should be an ADR. Never resolve a conflict by treating whichever text looks newer as true.
3. **Proof and gates:** vague, non-binary, step-based acceptance criteria; criteria that do not
   collectively prove the goal; or missing applicable completion criteria from `AGENTS.md`. Require
   nullness criteria for Java public API changes involving null-bearing types (ADR-009), and a
   `module-docs` deliverable plus checklist criterion for a new module or changed public packages,
   entry points, validate/read flows, non-goals, or agent-relevant invariants. Reference that skill;
   do not copy its checklist.
4. **Lifecycle and identity:**
   - rewriting an `In progress` plan instead of creating a follow-up;
   - any status other than `Not started` or `In progress`, including `Complete`;
   - checked acceptance criteria used as planning evidence;
   - phase-number identity in filenames, titles, or dependencies;
   - a `plans/README.md` or other plans-directory backlog/index;
   - a superseded original retained as an umbrella after reviewed replacements exist, or deleted
     while another live plan still references its filename, link, or relevant title. During the
     intermediate decomposition window, do not fail replacements merely because the still-present
     original has incoming references while reconciliation is unfinished.
5. **Dependencies and ownership:** dependencies must be hard execution-order prerequisites written
   as relative Markdown links to surviving live plan files, or `None`. Reject soft, optional, or
   parallel-override wording and Linear IDs, Outlook, deleted plans, bare titles, or bare path stems.
   Flag duplicated Vision, ADR, conformance, or module-docs prose that should be a canonical link.

For every actionable finding, provide a concise title, plan file/line evidence, affected planning
requirement, impact, concrete correction, and severity:

- **Critical:** fundamentally invalidates the plan or makes it unsafe to implement as written.
- **High:** a major execution-unit, lifecycle, gate, or unsurfaced authority conflict that would
  derail implementation or review.
- **Medium:** a real clarity, testability, or boundary gap with bounded impact.
- **Low:** a minor issue worth fixing; omit optional style preferences.

## Verdict

- **Pass:** no actionable findings remain and the contract satisfies the planning rules with
  sufficient repository evidence.
- **Changes required:** one or more actionable findings remain.
- **Blocked:** the review cannot reach a reliable conclusion because required repository evidence,
  dependencies, or lifecycle state is unavailable or ambiguous.

## Artifact

Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/implementation-plan-review-<plan-basename>.md
```

Use the template in [reference.md](reference.md).

The artifact is ephemeral and non-canonical. On every re-review, replace the prior artifact for that
plan instead of appending history. After writing it, report the artifact path and verdict to
the user.
