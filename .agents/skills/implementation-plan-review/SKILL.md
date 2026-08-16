---
name: implementation-plan-review
description: Optionally reviews whether a local working plan is a clear enough executable aide without material guessing or harmful over-specification. Writes an advisory assessment under `.agentWork/.session/`. Use when the user or planning explicitly requests an implementation plan review.
disable-model-invocation: true
---

# Implementation Plan Review

Ask: can an implementer proceed on the requested outcome without inventing **material**
product/design behavior? Flag over-specification as Advisory. Do not implement work, mutate the
plan, or gate Build.

Shared severity: [../review-findings.md](../review-findings.md). Artifact template:
[reference.md](reference.md). Design soundness is owned by `implementation-design-review`.

## Resolve inputs

Identify one local working plan under `.agentWork/plans/` (or supplied approach text). Ask when
ambiguous. Do not scan plans as a backlog. Read the plan, `AGENTS.md`, review-findings, and
narrowly implicated module/ADR/conformance evidence.

## Review

Exhaustive pass. Check goal coherence; approach vs repository reality; ownership/source-of-truth;
compatibility and migration obligations; scope discoverability; verification/checks; binary
acceptance intent; contradictions; gaps that force material guessing; over-prescription of harmless
local details (Advisory).

Do not require private helper structure, preselected method names, or exhaustive file-by-file
instructions. Do not become a second architecture contest; if a genuine unresolved architectural
choice appears, report it as a Blocking recommendation.

## Assessment

- **Unable to assess:** missing/ambiguous prerequisite evidence.
- **Concerns found:** material Blocking or Required concerns remain.
- **No material concerns:** otherwise (Advisory may remain).

## Artifact

Write `.agentWork/.session/implementation-plan-review-<basename>.md` using [reference.md](reference.md).
Report path and assessment. Do not mutate the plan.
