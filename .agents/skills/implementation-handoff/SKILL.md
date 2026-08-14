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

Derive `<basename>` from the plan file name without `.md`. For
`.agentWork/plans/jackson3-presence-aware-patch-binding.md`, basename is
`jackson3-presence-aware-patch-binding`.

Derived artifact paths (do not accept a free-form override):

- `implementation-design-review` (official pointer stub, written by the **orchestrating** session
  after both reviewers report — not by either reviewer session):
  `.agentWork/.session/implementation-design-review-<basename>.md`
- Design reviewer artifact:
  `.agentWork/.session/implementation-design-review-design-<basename>.md`
- Adversarial reviewer artifact:
  `.agentWork/.session/implementation-design-review-adversarial-<basename>.md`
- `implementation-plan-review` → `.agentWork/.session/implementation-plan-review-<basename>.md`
- `implementation-review` → `.agentWork/.session/implementation-review-<basename>.md`

## Write the handoff

1. Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/implementation-handoff-<review-kind>-<basename>.md
```

Use `design-review`, `plan-review`, or `implementation-review` as `<review-kind>`. For
`.agentWork/plans/jackson3-presence-aware-patch-binding.md` and an implementation review, write
`.agentWork/.session/implementation-handoff-implementation-review-jackson3-presence-aware-patch-binding.md`.

2. Fill the body for the suggested skill (below). Do **not** include planning or implementation
   narrative, diffs, self-assessment, or secrets.

3. Print the handoff path and the copy-pastable prompt(s) for the user.

### When suggested skill is `implementation-design-review`

This is the **terminating manual fallback**. Do **not** tell any fresh session to read
`implementation-design-review/SKILL.md` or re-run Orchestration (that recurses when spawning is
still unavailable).

`design.md` and `adversarial.md` are reviewer **procedure files**, not invokable skills.

Write this handoff body (fill `<plan path>` and `<basename>`):

~~~~markdown
# Implementation design-review handoff (manual fallback)

- **Plan:** `<plan path>`
- **Official stub (orchestrator only):** `.agentWork/.session/implementation-design-review-<basename>.md`
- **Design artifact:** `.agentWork/.session/implementation-design-review-design-<basename>.md`
- **Adversarial artifact:** `.agentWork/.session/implementation-design-review-adversarial-<basename>.md`

## Ownership

1. Run two independent fresh sessions using the prompts below (Design, then Adversarial, or in
   either order). Each session only reviews, writes **its own** artifact, and returns artifact
   path + verdict.
2. Neither reviewer session may read the other reviewer's artifact, perform worst-wins combine,
   or write the official pointer stub.
3. After both path + verdict results return, the **initiating/orchestrating session** resumes,
   combines only the two reported verdict strings (worst-wins), and writes the official stub.
4. If that orchestration session cannot be resumed, a separate mechanical combine-only step may
   consume only the two reported verdict strings and write the stub — it must not inspect
   findings, act as a third reviewer, or re-run design-review Orchestration.

### Worst-wins (orchestrator / mechanical combine-only only)

1. Any `Blocked`, or a missing/invalid verdict (not exactly `Pass` / `Changes required` /
   `Blocked`) → official `Blocked`
2. Else any `Changes required` → official `Changes required`
3. Else → `Pass`

## Design reviewer prompt (copy into a fresh session)

```text
You are the Design reviewer for this repository. Your context was intentionally started empty so
you review independently of the planning session.

Task inputs (the only facts you may assume):
- Plan: <plan path>
- Review artifact: .agentWork/.session/implementation-design-review-design-<basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/implementation-design-review/design.md and follow it exactly.
2. Base every conclusion only on the plan contract and repository evidence. Do not accept or
   ask for summaries from the planning session; ignore editor or IDE state.
3. Do not read adversarial.md, SKILL.md, or the other reviewer's artifact.
4. Do not perform worst-wins combine. Do not write the official pointer stub.
5. Write the artifact, then report the artifact path and verdict.
```

## Adversarial reviewer prompt (copy into a fresh session)

```text
You are the Adversarial reviewer for this repository. Your context was intentionally started empty
so you review independently of the planning session.

Task inputs (the only facts you may assume):
- Plan: <plan path>
- Review artifact: .agentWork/.session/implementation-design-review-adversarial-<basename>.md
  (create or completely replace)

Procedure:
1. Read .agents/skills/implementation-design-review/adversarial.md and follow it exactly.
2. Base every conclusion only on the plan contract and repository evidence. Do not accept or
   ask for summaries from the planning session; ignore editor or IDE state.
3. Do not read design.md, SKILL.md, or the other reviewer's artifact.
4. Do not perform worst-wins combine. Do not write the official pointer stub.
5. Write the artifact, then report the artifact path and verdict.
```
~~~~

Print both prompts (with `<plan path>` and `<basename>` filled). Do not print a one-liner that
invokes `implementation-design-review` as a skill.

### When suggested skill is `implementation-plan-review` or `implementation-review`

Write this body shape (fill **Review artifact** with the derived path):

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
