---
name: milestone-handoff
description: Writes a contract-only handoff under `.agentWork/.session/` so a fresh session can run an isolated milestone design, plan, or implementation review. Use when the harness cannot spawn a write-capable fresh subagent for design/plan/implementation review, or when the user asks for a milestone review handoff.
disable-model-invocation: true
---

# Milestone Handoff

Produce a contract-only handoff for an isolated milestone review. This is not general conversation
compaction: include only what a fresh session needs to invoke the review skill against repository
evidence.

## Resolve inputs

Require both of:

- **Milestone path** under `.agentWork/milestones/`
- **Suggested skill:** `milestone-design-review`, `milestone-plan-review`, or `milestone-review`

Ask the user when either input is missing or ambiguous.

Derive the review artifact path from the suggested skill and the milestone basename (do not accept a
free-form override):

- `milestone-design-review` → `.agentWork/.session/milestone-design-review-<milestone-basename>.md`
- `milestone-plan-review` → `.agentWork/.session/milestone-plan-review-<milestone-basename>.md`
- `milestone-review` → `.agentWork/.session/milestone-review-<milestone-basename>.md`

For `.agentWork/milestones/phase-1-1-spec-data-model.md` with `milestone-review`, the derived path is
`.agentWork/.session/milestone-review-phase-1-1-spec-data-model.md`. For `milestone-design-review`,
the derived path is the official pointer stub; the fresh session runs design-review orchestration
(two reviewers plus stub), not a single reviewer procedure.

## Write the handoff

1. Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/milestone-handoff-<review-kind>-<milestone-basename>.md
```

Use `design-review`, `plan-review`, or `implementation-review` as `<review-kind>`. For
`.agentWork/milestones/phase-1-1-spec-data-model.md` and an implementation review, write
`.agentWork/.session/milestone-handoff-implementation-review-phase-1-1-spec-data-model.md`.

2. Use this body shape (fill **Review artifact** with the derived path):

```markdown
# Milestone review handoff

- **Milestone:** `<milestone path>`
- **Review artifact:** `<derived artifact path>`
- **Suggested skill:** `<milestone-design-review | milestone-plan-review | milestone-review>`

## Instructions

Start a fresh session. Read `.agents/skills/<suggested-skill>/SKILL.md` and follow it exactly.
Derive every conclusion from the milestone contract and repository evidence. Do not accept
summaries, narrative, diffs, or self-assessment from the prior session.
```

3. Do **not** include planning or implementation narrative, diffs, self-assessment, or secrets.

4. Print the handoff path and the exact one-liner the user should run to start the review in a new
   session (invoke the suggested skill with the milestone path).
