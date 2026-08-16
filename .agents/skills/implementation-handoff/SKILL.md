---
name: implementation-handoff
description: Writes a contract-only handoff under `.agentWork/.session/` so a fresh session can run an isolated design, plan, or implementation review. Use when the harness cannot spawn a write-capable fresh subagent for review, or when the user asks for a review handoff.
disable-model-invocation: true
---

# Implementation Handoff

Produce a contract-only handoff for an isolated review. Include only what a fresh session needs
against repository evidence—no implementation narrative, diffs, or secrets.

## Resolve inputs

Require:

- **Suggested skill:** `implementation-design-review`, `implementation-plan-review`, or
  `implementation-review`
- For design review: design source path and/or full embedded design text (plan-less reviews must
  carry the actual design, not only a missing plan path)
- For plan review: **requested outcome**, **acceptance intent**, and plan/approach source path
  (materialize plan-less approach text to `.agentWork/.session/plan-source-<basename>.md` when
  needed)
- For implementation review: **requested outcome** and **acceptance intent** (required). Prefer
  embedding them in the handoff prompt. Write `.agentWork/.session/work-context.md` only when the
  harness cannot pass intent otherwise.

Ask when inputs are missing. Derive `<basename>` and artifact paths from
[../implementation-design-review/reference.md](../implementation-design-review/reference.md).

## Design review handoff

Default: one Design reviewer prompt from the design-review reference, with the same design-source
transport rules (plan path, session `design-source-*` path, or embedded Design text). Include the
optional Adversarial prompt only when that lens was requested. Do not treat reviewer assessments as
Build permission.

## Plan review handoff

```markdown
# Implementation plan-review handoff

- **Requested outcome:** `<outcome>`
- **Acceptance intent:** `<acceptance intent>`
- **Plan / approach source:** `<path>`
- **Review artifact:** `<derived artifact path>`
- **Suggested skill:** `implementation-plan-review`

## Instructions

Start a fresh session. Read `.agents/skills/implementation-plan-review/SKILL.md` and follow the
Reviewer procedure exactly. Use only the supplied outcome, acceptance intent, plan/approach source,
and repository evidence. Do not mutate the plan. Report the artifact path and assessment.
```

Prefer embedding the filled Plan Review spawn prompt from
[../implementation-plan-review/reference.md](../implementation-plan-review/reference.md).

## Implementation review handoff

```markdown
# Implementation review handoff

- **Requested outcome:** `<outcome>`
- **Acceptance intent:** `<acceptance intent>`
- **Review artifact:** `<derived artifact path>`
- **Optional local plan (context only):** `<path or none>`
- **Work-context fallback:** `<path or none>`
- **Suggested skill:** `implementation-review`

## Instructions

Start a fresh session. Read `.agents/skills/implementation-review/SKILL.md` and follow it exactly.
Evaluate requested outcome / acceptance intent + actual diff + repository contracts/docs/tests +
applicable gates. Do not reconstruct the requested work from the local plan, branch names,
trackers, Git history, or inferred code alone.
```

Print the handoff path and copy-pastable prompt(s). Stop and wait for the fresh-session result.
