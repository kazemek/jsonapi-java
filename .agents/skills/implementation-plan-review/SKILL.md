---
name: implementation-plan-review
description: Optionally reviews whether a local working plan or approach is a clear enough executable aid without material guessing or harmful over-specification. Spawns a fresh-context reviewer when the harness supports it and writes an advisory assessment under `.agentWork/.session/`. Use when the user or planning explicitly requests an implementation plan review.
disable-model-invocation: true
---

# Implementation Plan Review

Ask: can an implementer proceed on the requested outcome without inventing **material**
product/design behavior? Flag over-specification as Advisory. Do not implement work, mutate the
plan, or gate Build. Findings are advisory recommendations to the maintainer.

Shared severity: [../review-findings.md](../review-findings.md). Spawn prompt and artifact
template: [reference.md](reference.md). Design soundness is owned by `implementation-design-review`.

## Resolve inputs

Require:

- **Requested outcome**
- **Acceptance intent**
- **Local plan / approach source** — a path under `.agentWork/plans/`, or supplied approach text
  (materialize plan-less text into `.agentWork/.session/` per [reference.md](reference.md) when a
  path is needed for the fresh reviewer)

Ask when ambiguous. Do not scan `.agentWork/plans/` as a backlog.

## Orchestration

When the harness can spawn a write-capable fresh subagent, spawn **one** NEW general-purpose
subagent with empty context and send the Plan Review prompt from [reference.md](reference.md)
verbatim (placeholders only). Prefer embedding outcome, acceptance intent, and approach source in
that prompt.

If a fresh write-capable subagent cannot run, follow
[../implementation-handoff/SKILL.md](../implementation-handoff/SKILL.md) for
`implementation-plan-review`.

Never auto-apply findings. Never auto re-review. Present assessments to the maintainer for
apply / reject / discuss. Do not mutate the plan.

## Reviewer procedure

When acting as the fresh reviewer (or after reading the skill in a handoff session):

1. Read the supplied outcome, acceptance intent, and plan/approach source from the task inputs.
2. Read `AGENTS.md`, [../review-findings.md](../review-findings.md), and narrowly implicated
   module/ADR/conformance evidence.
3. Exhaustive pass: goal coherence; approach vs repository reality; ownership/source-of-truth;
   compatibility and migration obligations; scope discoverability; verification/checks; binary
   acceptance intent; contradictions; gaps that force material guessing; over-prescription of
   harmless local details (Advisory).
4. Do not require private helper structure, preselected method names, or exhaustive file-by-file
   instructions. Do not become a second architecture contest; if a genuine unresolved architectural
   choice appears, report it as a Blocking recommendation.
5. Write `.agentWork/.session/implementation-plan-review-<basename>.md` using the template in
   [reference.md](reference.md). Report path and assessment. Do not mutate the plan.

## Assessment

- **Unable to assess:** missing/ambiguous prerequisite evidence.
- **Concerns found:** material Blocking or Required concerns remain.
- **No material concerns:** otherwise (Advisory may remain).
