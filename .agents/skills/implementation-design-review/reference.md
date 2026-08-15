# Design Review Shared Reference

This file is the canonical local owner of design-review launcher prompts, fixed artifact paths,
official pointer-stub shape, and verdict-combination data. Orchestration and fallback state
transitions remain in their respective skills. Finding severity lives in
[../review-findings.md](../review-findings.md). Review epochs and automatic budgets live in
`implementation-planning`.

## Placeholders and paths

Derive `<basename>` from the plan file name without `.md`. Derive `<plan title>` from the plan's
top-level title.

| Artifact | Fixed path |
|----------|------------|
| Official design-review pointer stub | `.agentWork/.session/implementation-design-review-<basename>.md` |
| Design reviewer artifact | `.agentWork/.session/implementation-design-review-design-<basename>.md` |
| Adversarial reviewer artifact | `.agentWork/.session/implementation-design-review-adversarial-<basename>.md` |
| Design-review epoch ledger | `.agentWork/.session/review-epoch-design-<basename>.md` |
| Plan-review epoch ledger | `.agentWork/.session/review-epoch-plan-<basename>.md` |
| Plan-review artifact | `.agentWork/.session/implementation-plan-review-<basename>.md` |
| Implementation-review artifact | `.agentWork/.session/implementation-review-<basename>.md` |
| Design-review handoff | `.agentWork/.session/implementation-handoff-design-review-<basename>.md` |
| Plan-review handoff | `.agentWork/.session/implementation-handoff-plan-review-<basename>.md` |
| Implementation-review handoff | `.agentWork/.session/implementation-handoff-implementation-review-<basename>.md` |

Archive copies (before replacing fixed paths for a new attempt) use:

```text
.agentWork/.session/archive/<artifact-stem>-e<epoch>-a<attempt>.md
```

where `<artifact-stem>` is the fixed filename without `.md` (for example
`implementation-design-review-design-<basename>`).

Artifact paths are fixed; do not accept free-form overrides. Create `.agentWork/.session/` when
needed. Current fixed paths always hold the latest attempt; archives preserve history.

## Reviewer prompts

Replace only `<plan path>` and `<basename>`. Send or embed every other character verbatim. Do not
add summaries, self-assessment, reasoning, planning narrative, or draft diffs.

### Design

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

### Adversarial

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

## Combine data and ownership

Combine only the two verdict strings reported by the reviewers. Do not parse artifact `Verdict:`
headers, invent a Pass, or re-score severity. `Required` and `Advisory` findings do not change the
combined verdict; only reported `Changes required` / `Blocked` / `Pass` strings do.

Apply this exact precedence:

1. If either reported verdict is `Blocked`, missing, or not exactly `Pass`, `Changes required`, or
   `Blocked`, the official verdict is `Blocked`.
2. Else if either verdict is `Changes required`, the official verdict is `Changes required`.
3. Else the official verdict is `Pass`.

Neither reviewer may combine verdicts or write the official pointer stub. The initiating
orchestrator is the normal owner of both operations. The handoff fallback may assign them to a
separate mechanical combine-only step, which may consume only the two reported verdict strings,
apply the precedence above, and write the stub. That step must not inspect findings, act as a third
reviewer, or run design-review orchestration.

## Pointer stub

Create or completely replace the official pointer-stub path with exactly this shape:

```markdown
# Design review: <plan title>

- **Plan:** `<plan path>`
- **Epoch / attempt:** <epoch> / <attempt>
- **Design:** <Pass | Changes required | Blocked> — `.agentWork/.session/implementation-design-review-design-<basename>.md`
- **Adversarial:** <Pass | Changes required | Blocked> — `.agentWork/.session/implementation-design-review-adversarial-<basename>.md`
- **Official:** <Pass | Changes required | Blocked>
```

Fill **Epoch / attempt** from the design epoch ledger when present (planning-managed). For on-demand
design review with no ledger, write `on-demand / 1`. The stub is paths and verdicts only. Do not add
a summary, findings, or residual risks; those remain in the reviewer artifacts. Plan review reads
those artifacts for unresolved `Required` carry-forward.
