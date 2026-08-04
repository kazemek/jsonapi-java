---
name: milestone-handoff
description: Writes a contract-only handoff under `.agentWork/.session/` so a fresh session can run an isolated milestone plan or implementation review. Use when the harness cannot spawn a write-capable fresh subagent for plan/implementation review, or when the user asks for a milestone review handoff.
disable-model-invocation: true
---

# Milestone Handoff

Produce a contract-only handoff for an isolated milestone review. This is not general conversation
compaction: include only what a fresh session needs to invoke the review skill against repository
evidence.

## Resolve inputs

Require all of:

- **Milestone path** under `.agentWork/milestones/`
- **Review artifact path** under `.agentWork/.session/` (the path the reviewer will create or
  replace)
- **Suggested skill:** `milestone-review` or `milestone-plan-review`

Ask the user when any input is missing or ambiguous.

## Write the handoff

1. Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/milestone-handoff-<review-kind>-<milestone-basename>.md
```

Use `plan-review` or `implementation-review` as `<review-kind>`. For
`.agentWork/milestones/phase-1-1-spec-data-model.md` and an implementation review, write
`.agentWork/.session/milestone-handoff-implementation-review-phase-1-1-spec-data-model.md`.

2. Use this body shape:

```markdown
# Milestone review handoff

- **Milestone:** `<milestone path>`
- **Review artifact:** `<artifact path>`
- **Suggested skill:** `<milestone-review | milestone-plan-review>`

## Instructions

Start a fresh session. Read `.agents/skills/<suggested-skill>/SKILL.md` and follow it exactly.
Derive every conclusion from the milestone contract and repository evidence. Do not accept
summaries, narrative, diffs, or self-assessment from the prior session.
```

3. Do **not** include planning or implementation narrative, diffs, self-assessment, or secrets.

4. Print the handoff path and the exact one-liner the user should run to start the review in a new
   session (invoke the suggested skill with the milestone path).
