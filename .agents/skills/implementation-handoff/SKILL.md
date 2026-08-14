---
name: implementation-handoff
description: Writes a contract-only handoff under `.agentWork/.session/` so a fresh session can run an isolated implementation design, plan, or implementation review. Use when the harness cannot spawn a write-capable fresh subagent for design/plan/implementation review, or when the user asks for an implementation review handoff.
disable-model-invocation: true
---

# Implementation Handoff

Produce a contract-only handoff for an isolated implementation review. This is not general
conversation compaction: include only what a fresh session needs against repository evidence.

## Resolve inputs

Require both of:

- **Plan path** under `.agentWork/plans/`
- **Suggested skill:** `implementation-design-review`, `implementation-plan-review`, or
  `implementation-review`

Ask the user when either input is missing or ambiguous.

Derive `<basename>` and all artifact paths from the canonical
[design-review reference](../implementation-design-review/reference.md). Do not accept free-form
path overrides.

## Write the handoff

1. Create `.agentWork/.session/` if needed, then create or completely replace the fixed handoff path
   for the selected review kind in the
   [design-review reference](../implementation-design-review/reference.md).

2. Fill the body for the suggested skill (below). Do **not** include planning or implementation
   narrative, diffs, self-assessment, or secrets.

3. Print the handoff path and the copy-pastable prompt(s) for the user.

### When suggested skill is `implementation-design-review`

This is the **terminating manual fallback**. Write this handoff body, filling `<plan path>`,
`<basename>`, and all paths from the
[design-review reference](../implementation-design-review/reference.md). Embed both reviewer prompt
blocks from that reference verbatim after replacing only their placeholders.

~~~~markdown
# Implementation design-review handoff (manual fallback)

- **Plan:** `<plan path>`
- **Official stub (orchestrator only):** `<official pointer-stub path from reference.md>`
- **Design artifact:** `<Design reviewer artifact path from reference.md>`
- **Adversarial artifact:** `<Adversarial reviewer artifact path from reference.md>`

## Ownership

1. Run the two prompts below in independent fresh, write-capable sessions, in either order or in
   parallel. Each reviewer follows only its prompt, writes its own artifact, and returns artifact
   path + verdict. Do not ask either session to read
   `.agents/skills/implementation-design-review/SKILL.md`; that would recurse into orchestration.
   Neither reviewer may read the other artifact, combine verdicts, or write the official stub.
2. After both results return, resume the initiating/orchestrating session. It applies **Combine data
   and ownership** from `.agents/skills/implementation-design-review/reference.md` and writes the
   official stub using that reference's exact shape.
3. If the initiating session cannot be resumed, use only the mechanical combine-only fallback
   permitted by that reference. Do not start another reviewer or orchestration session.

## Design reviewer prompt (copy into a fresh session)

<the filled Design prompt from `.agents/skills/implementation-design-review/reference.md`, verbatim>

## Adversarial reviewer prompt (copy into a fresh session)

<the filled Adversarial prompt from `.agents/skills/implementation-design-review/reference.md`, verbatim>
~~~~

Print both prompts (with `<plan path>` and `<basename>` filled). Do not print a one-liner that
invokes `implementation-design-review` as a skill. Stop spawning and wait for both reviewer results.

### When suggested skill is `implementation-plan-review` or `implementation-review`

Write this body shape (fill **Review artifact** with the fixed path from the
[design-review reference](../implementation-design-review/reference.md)):

```markdown
# Implementation review handoff

- **Plan:** `<plan path>`
- **Review artifact:** `<derived artifact path>`
- **Suggested skill:** `<implementation-plan-review | implementation-review>`

## Instructions

Start a fresh session. Read `.agents/skills/<suggested-skill>/SKILL.md` and follow it exactly.
Derive every conclusion from the plan contract and repository evidence. Do not accept
summaries, narrative, diffs, or self-assessment from the prior session.
```

Print the handoff path and the exact one-liner to invoke the suggested skill with the plan path.
