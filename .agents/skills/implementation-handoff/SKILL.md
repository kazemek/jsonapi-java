---
name: implementation-handoff
description: Writes a contract-only handoff under `.agentWork/.session/` so a fresh session can run an isolated implementation design, plan, or implementation review. Use when the harness cannot spawn a write-capable fresh subagent for design/plan/implementation review, or when the user asks for an implementation review handoff.
disable-model-invocation: true
---

# Implementation Handoff

Produce a contract-only handoff for an isolated implementation review. This is not general
conversation compaction: include only what a fresh session needs to invoke the review skill against
repository evidence.

## Resolve inputs

Require both of:

- **Plan path** under `.agentWork/plans/`
- **Suggested skill:** `implementation-design-review`, `implementation-plan-review`, or
  `implementation-review`

Ask the user when either input is missing or ambiguous.

Derive the review artifact path from the suggested skill and the plan basename (do not accept a
free-form override):

- `implementation-design-review` → `.agentWork/.session/implementation-design-review-<plan-basename>.md`
- `implementation-plan-review` → `.agentWork/.session/implementation-plan-review-<plan-basename>.md`
- `implementation-review` → `.agentWork/.session/implementation-review-<plan-basename>.md`

For `.agentWork/plans/jackson3-presence-aware-patch-binding.md` with `implementation-review`, the
derived path is `.agentWork/.session/implementation-review-jackson3-presence-aware-patch-binding.md`.
For `implementation-design-review`, the derived path is the official pointer stub; the fresh session
runs design-review orchestration (two reviewers plus stub), not a single reviewer procedure.

## Write the handoff

1. Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/implementation-handoff-<review-kind>-<plan-basename>.md
```

Use `design-review`, `plan-review`, or `implementation-review` as `<review-kind>`. For
`.agentWork/plans/jackson3-presence-aware-patch-binding.md` and an implementation review, write
`.agentWork/.session/implementation-handoff-implementation-review-jackson3-presence-aware-patch-binding.md`.

2. Use this body shape (fill **Review artifact** with the derived path):

```markdown
# Implementation review handoff

- **Plan:** `<plan path>`
- **Review artifact:** `<derived artifact path>`
- **Suggested skill:** `<implementation-design-review | implementation-plan-review | implementation-review>`

## Instructions

Start a fresh session. Read `.agents/skills/<suggested-skill>/SKILL.md` and follow it exactly.
Derive every conclusion from the plan contract and repository evidence. Do not accept
summaries, narrative, diffs, or self-assessment from the prior session.
```

3. Do **not** include planning or implementation narrative, diffs, self-assessment, or secrets.

4. Print the handoff path and the exact one-liner the user should run to start the review in a new
   session (invoke the suggested skill with the plan path).
