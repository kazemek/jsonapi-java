---
name: implementation-design-review
description: Optionally reviews whether a proposed technical design is sound using one fresh Design Reviewer (optional second Adversarial reviewer when requested). Writes advisory findings under `.agentWork/.session/`. Use when the user or planning explicitly requests an implementation design review.
disable-model-invocation: true
---

# Implementation Design Review

Challenge whether the proposed design is coherent, appropriately simple, compatible with current
architecture, and preferable to credible simpler alternatives. Do not implement work, mutate plans,
or gate Build. Findings are advisory recommendations to the maintainer.

Reviewer procedure: [design.md](design.md). Optional second lens: [adversarial.md](adversarial.md).
Paths and prompts: [reference.md](reference.md). Severity: [../review-findings.md](../review-findings.md).

## Resolve inputs

Identify the local working plan path under `.agentWork/plans/` when one exists, or the design
approach text supplied by the user. Ask when ambiguous. Do not scan `.agentWork/plans/` as a backlog.

## Orchestration

Default: spawn **one** NEW general-purpose, write-capable subagent with fresh context. Send the
Design prompt from [reference.md](reference.md) verbatim (placeholders only).

Optional second Adversarial reviewer only when the maintainer requests it or agrees to a planner
recommendation for unusually cross-cutting or hard-to-reverse work. Run in parallel when both run.

Never auto-apply findings. Do not combine verdicts into a workflow gate. Present assessments to the
maintainer for apply / reject / discuss.

If a fresh write-capable subagent cannot run, follow
[../implementation-handoff/SKILL.md](../implementation-handoff/SKILL.md).

## After review

Report artifact path(s) and assessment. Stop. Do not fix the plan and do not run Plan Review or
Build unless the user separately asks.
