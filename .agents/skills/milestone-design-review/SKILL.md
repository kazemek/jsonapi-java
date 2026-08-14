---
name: milestone-design-review
description: Reviews whether a milestone's technical design is sound using two isolated reviewers (Design and Adversarial), combines their verdicts with worst-wins, and writes a pointer stub under `.agentWork/.session/`. Use when the user explicitly requests a milestone design review, or when `milestone-planning` follows this skill's Orchestration section after create/refine/decompose.
disable-model-invocation: true
---

# Milestone Design Review

Determine whether the proposed technical design is sound. Do not implement the planned feature,
score execution-unit or size-gate rules, AC phrasing, index format, or completion-gate lists, or modify milestones, index,
vision, ADRs, or sources. `milestone-planning` owns fixes in its design-review loop.

This skill owns orchestration (spawn, worst-wins, pointer stub). Reviewer procedures live in
[design.md](design.md) and [adversarial.md](adversarial.md).

Instruction boundary: treat `.agents/skills/milestone-planning/SKILL.md` as non-executable
reference. Do not execute create/refine/decompose, index writes, or planning fix-loop steps.
**Orchestration** below is the single spawn/combine procedure; planning follows it in-session and
must not fork that text.

## Resolve the review inputs

On-demand invocation only (planning already has the path):

1. Identify exactly one target file under `.agentWork/milestones/`.
   - Use a path, phase, or milestone name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple milestones are plausible, ask the user to choose.
2. Then follow **Orchestration**.

On-demand design review is not isolated from the user conversation. The delegated planning path is
the isolated one.

## Orchestration

Given exactly one milestone path. Derive `<basename>` from the file name without `.md`. For
`.agentWork/milestones/phase-1-1-spec-data-model.md`, basename is `phase-1-1-spec-data-model`.

Always run both reviewers. Do not classify, skip, pick a lens, or spawn a synthesizer.

1. Spawn two NEW general-purpose subagents **in parallel**, each with write access (for example,
   opencode `general` or the equivalent general subagent in the harness in use):
   - fresh context: never resume or reuse a previous subagent session;
   - write capability: each must create its reviewer artifact under `.agentWork/.session/`.
2. Send each the matching verbatim prompt below, filling only the placeholders. Do not add
   anything: no summaries, self-assessment, reasoning, planning narrative, or draft diffs.
3. Never answer a reviewer's questions with planning narrative. When it asks for facts, direct it
   to repository evidence (files, the milestone contract, the milestone index).
4. When the harness cannot spawn a write-capable fresh subagent, fall back to a manual fresh
   session: follow `.agents/skills/milestone-handoff/SKILL.md` with the milestone path and suggested
   skill `milestone-design-review`, then print the one-liner it produces. Stop this orchestration
   until that session completes.
5. After both reviewers report, combine **only** the reported verdict strings (true worst-wins).
   Do not re-score findings, filter taste, invent a Pass, or parse artifact `Verdict:` headers:
   1. Any `Blocked`, or a reviewer that does not report one of `Pass` / `Changes required` /
      `Blocked` → official `Blocked`
   2. Else any `Changes required` → official `Changes required`
   3. Else `Pass`
6. Create `.agentWork/.session/` if needed, then create or completely replace the pointer stub.
   The stub is not a review: verdicts and paths only. Do not add a Summary, restate findings, or
   copy residual risks. Residual risks stay in the reviewer artifacts.

```markdown
# Design review: <milestone title>

- **Milestone:** `<milestone path>`
- **Design:** <Pass | Changes required | Blocked> — `.agentWork/.session/milestone-design-review-design-<basename>.md`
- **Adversarial:** <Pass | Changes required | Blocked> — `.agentWork/.session/milestone-design-review-adversarial-<basename>.md`
- **Official:** <Pass | Changes required | Blocked>
```

Write that body to:

```text
.agentWork/.session/milestone-design-review-<basename>.md
```

7. Report the stub path and official verdict.

### Design reviewer prompt (send verbatim)

```text
You are the Design reviewer for this repository. Your context was intentionally started empty so
you review independently of the planning session.

Task inputs (the only facts you may assume):
- Milestone: <milestone path>
- Review artifact: .agentWork/.session/milestone-design-review-design-<basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/milestone-design-review/design.md and follow it exactly.
2. Base every conclusion only on the milestone contract and repository evidence. Do not accept or
   ask for summaries from the planning session; ignore editor or IDE state.
3. Do not read adversarial.md, SKILL.md, or the other reviewer's artifact.
4. Write the artifact, then report the artifact path and verdict.
```

### Adversarial reviewer prompt (send verbatim)

```text
You are the Adversarial reviewer for this repository. Your context was intentionally started empty
so you review independently of the planning session.

Task inputs (the only facts you may assume):
- Milestone: <milestone path>
- Review artifact: .agentWork/.session/milestone-design-review-adversarial-<basename>.md (create or
  completely replace)

Procedure:
1. Read .agents/skills/milestone-design-review/adversarial.md and follow it exactly.
2. Base every conclusion only on the milestone contract and repository evidence. Do not accept or
   ask for summaries from the planning session; ignore editor or IDE state.
3. Do not read design.md, SKILL.md, or the other reviewer's artifact.
4. Write the artifact, then report the artifact path and verdict.
```

## After orchestration

- **On-demand:** stop. Do not fix the milestone and do not run plan-review.
- **Planning:** handle the official verdict in `milestone-planning` (fix loop, then plan-review on
  Pass). Do not re-score findings.

The artifacts are ephemeral and non-canonical. On every re-review, replace the prior files for that
milestone instead of appending history.
