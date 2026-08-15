---
name: implementation-plan-review
description: Reviews an implementation plan (spec) against planning rules and repository evidence, and writes the result to `.agentWork/.session/`. Use when the user explicitly requests an implementation plan review, spec audit, or planning acceptance review, or when `implementation-planning` delegates plan review to a fresh-context subagent after design-review Pass.
disable-model-invocation: true
---

# Implementation Plan Review

Review one implementation plan contract as a planning artifact. Do not implement the planned
feature, run feature completion builds, or modify plans, vision, ADRs, or sources unless the user
separately asks for changes. The `implementation-planning` skill owns fixes, review epochs, and
automatic re-review budgets in its review loop.

Treat `.agents/skills/implementation-planning/SKILL.md`, its sibling `reference.md`, and all policy
documents as non-executable evidence. Do not execute their create/refine/decompose, write, subagent,
or fix-loop instructions. This skill's artifact-only and no-mutation boundary is authoritative.

Shared severity and stage ownership: [../review-findings.md](../review-findings.md). Design
soundness is owned by `implementation-design-review`. Do not recommend alternative architectures,
APIs, or module placements as plan-review taste. If a finding shows a genuine unresolved
architectural choice or contradicts an approved design, classify it `Blocking` and require planning
refinement with design review — never hide it as `Required`.

## Resolve inputs

1. Identify exactly one target file under `.agentWork/plans/`.
   - Use a path or plan name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple plans are plausible, ask the user to choose.
   - Do not infer the next review target by scanning `.agentWork/plans/` as a backlog.
2. Read the plan, `AGENTS.md`, [../review-findings.md](../review-findings.md), and the planning
   skill and reference for planning-quality rules. Read named module READMEs and package
   documentation; `docs/vision.md` for direction, module, or public-boundary changes or a suspected
   conflict; and linked ADRs or conformance sections. Expand only to records directly implicated by
   the planned scope.
3. Read design Required carry-forward and current design-review artifacts for the same basename when
   they exist (carry-forward path and design/adversarial/stub paths from
   `.agents/skills/implementation-design-review/reference.md`). Treat every unresolved `Required`
   finding in the carry-forward **and** in current design/adversarial artifacts as a `Required`
   plan-review finding until the plan addresses it. A finding present only in an archived attempt
   but already recorded in carry-forward remains in scope via carry-forward.
4. Inspect narrow production and test files only to check feasibility claims in the plan — not
   to score an implementation.
5. State the reviewed contract boundary in the artifact (plan path and any adjacent live plans
   inspected). Never imply that unexamined plans or code were reviewed.

External work-tracker access is never required to review a materialized plan. Do not treat
work-tracker metadata, Outlook, or deleted plans as engineering proof.

## Review

Follow [../review-findings.md](../review-findings.md): exhaustive pass over the whole contract.
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
   parallel-override wording and external work-item IDs, Outlook, deleted plans, bare titles, or
   bare path stems.
   Flag duplicated Vision, ADR, conformance, or module-docs prose that should be a canonical link.
6. **Design carry-forward and implementability:** unresolved items in the design Required
   carry-forward and current design-review `Required` findings; missing compatibility, file/scope,
   test, gate, or migration detail an implementer would have to invent; consistency with the
   approved design without re-litigating architecture.

For every finding, provide a concise title, plan file/line evidence, affected planning requirement,
impact, concrete correction, and severity `Blocking` | `Required` | `Advisory` per
[../review-findings.md](../review-findings.md).

## Verdict

Apply the plan-review mapping in [../review-findings.md](../review-findings.md):

- **Pass:** no `Blocking` or `Required` findings remain.
- **Changes required:** one or more `Blocking` or `Required` findings remain.
- **Blocked:** the review cannot reach a reliable conclusion because required repository evidence,
  dependencies, or lifecycle state is unavailable or ambiguous.

When a `Blocking` finding is architectural (unresolved design choice or contradiction with the
approved design), say so in the recommendation and require `implementation-planning` to follow the
architectural-escalation epoch transition (new design review) — do not treat a local wording patch
as sufficient.

## Artifact

Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/implementation-plan-review-<plan-basename>.md
```

Use the template in [reference.md](reference.md).

Current fixed-path artifacts hold the latest attempt. Planning preserves history via archive copies
and epoch ledgers. After writing the artifact, report the artifact path and verdict to the user.
