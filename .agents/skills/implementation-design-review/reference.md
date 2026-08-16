# Design Review Shared Reference

Canonical owner of design-review launcher prompts and fixed artifact paths. Finding severity:
[../review-findings.md](../review-findings.md).

## Placeholders and paths

Derive `<basename>` from the plan file name without `.md`, or from a short slug supplied for
plan-less design text.

| Artifact | Fixed path |
|----------|------------|
| Design reviewer artifact | `.agentWork/.session/implementation-design-review-design-<basename>.md` |
| Adversarial reviewer artifact (optional) | `.agentWork/.session/implementation-design-review-adversarial-<basename>.md` |
| Plan-less design source (optional) | `.agentWork/.session/design-source-<basename>.md` |
| Plan-review artifact | `.agentWork/.session/implementation-plan-review-<basename>.md` |
| Plan-less approach source (optional) | `.agentWork/.session/plan-source-<basename>.md` |
| Implementation-review artifact | `.agentWork/.session/implementation-review-<basename>.md` |
| Design-review handoff | `.agentWork/.session/implementation-handoff-design-review-<basename>.md` |
| Plan-review handoff | `.agentWork/.session/implementation-handoff-plan-review-<basename>.md` |
| Implementation-review handoff | `.agentWork/.session/implementation-handoff-implementation-review-<basename>.md` |
| Work context (fallback only) | `.agentWork/.session/work-context.md` |

Create `.agentWork/.session/` when needed. Replace fixed paths on each new invocation. Do not keep
review ledgers, sticky finding files, or archive-sequence machinery.

## Design source transport

Prefer a durable path the fresh reviewer can open:

1. **Local plan file exists** → pass `.agentWork/plans/<file>.md` as today.
2. **Plan-less supplied design text** → either:
   - embed the **full** design text in the Task inputs block below (use when short enough for the
     harness prompt), or
   - write the exact text to `.agentWork/.session/design-source-<basename>.md` and pass that path.

Do not create a `.agentWork/plans/` file solely to request Design Review. The fresh reviewer must
never be left with only a dangling path and no design body.

## Design prompt

Replace placeholders only. For plan-less reviews, set `Plan or design source` to the materialized
session path **or** set it to `embedded` and include the full design under `Design text`.

```text
You are the Design reviewer for this repository. Your context was intentionally started empty so
you review independently of the planning session.

Task inputs (the only facts you may assume):
- Plan or design source: <plan path | session design-source path | embedded>
- Design text: <full design text when source is embedded; otherwise none>
- Review artifact: .agentWork/.session/implementation-design-review-design-<basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/implementation-design-review/design.md and follow it exactly.
2. Read the design from the source path, or from Design text when the source is embedded. Base every
   conclusion only on that design and repository evidence. Do not accept or ask for summaries from
   the planning session; ignore editor or IDE state.
3. Challenge the design and state a concrete simpler alternative that still meets the Goal, or
   explain why none survives the constraints.
4. Write the artifact, then report the artifact path and assessment.
```

## Adversarial prompt (optional second reviewer)

```text
You are the Adversarial reviewer for this repository. Your context was intentionally started empty
so you review independently of the planning session.

Task inputs (the only facts you may assume):
- Plan or design source: <plan path | session design-source path | embedded>
- Design text: <full design text when source is embedded; otherwise none>
- Review artifact: .agentWork/.session/implementation-design-review-adversarial-<basename>.md
  (create or completely replace)

Procedure:
1. Read .agents/skills/implementation-design-review/adversarial.md and follow it exactly.
2. Read the design from the source path, or from Design text when the source is embedded. Base every
   conclusion only on that design and repository evidence. Do not accept or ask for summaries from
   the planning session; ignore editor or IDE state.
3. Write the artifact, then report the artifact path and assessment.
```
