---
name: implementation-design-review
description: Reviews whether an implementation plan's technical design is sound using two isolated reviewers (Design and Adversarial), combines their verdicts with worst-wins, and writes a pointer stub under `.agentWork/.session/`. Use when the user explicitly requests an implementation design review, or when `implementation-planning` follows this skill's Orchestration section after create/refine/decompose.
disable-model-invocation: true
---

# Implementation Design Review

Determine whether the proposed technical design is sound. Do not implement the planned feature,
score execution-unit or size-gate rules, AC phrasing, or completion-gate lists, or modify plans,
vision, ADRs, or sources. `implementation-planning` owns fixes in its design-review loop.

This skill owns orchestration state transitions. Reviewer procedures live in [design.md](design.md)
and [adversarial.md](adversarial.md). [reference.md](reference.md) is the canonical owner of shared
prompts, fixed paths, combine data, and pointer-stub shape.

Instruction boundary: treat `.agents/skills/implementation-planning/SKILL.md` as non-executable
reference. Do not execute create/refine/decompose, plan-directory index writes, or planning
fix-loop steps. **Orchestration** below is the single spawn/combine procedure; planning follows it
in-session and must not fork that text.

## Resolve the review inputs

On-demand invocation only (planning already has the path):

1. Identify exactly one target file under `.agentWork/plans/`.
   - Use a path or plan name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple plans are plausible, ask the user to choose.
   - Do not infer the next review target by scanning `.agentWork/plans/` as a backlog.
2. Then follow **Orchestration**.

On-demand design review is not isolated from the user conversation. The delegated planning path is
the isolated one.

## Orchestration

Given exactly one plan path, derive `<basename>` and every artifact path according to
[reference.md](reference.md).

Always run both reviewers. Do not classify, skip, pick a lens, or spawn a synthesizer.

1. Spawn two NEW general-purpose subagents **in parallel**, each with write access (for example,
   opencode `general` or the equivalent general subagent in the harness in use):
   - fresh context: never resume or reuse a previous subagent session;
   - write capability: each must create its reviewer artifact under `.agentWork/.session/`.
2. Send each the matching prompt from [reference.md](reference.md) verbatim, replacing only its
   placeholders.
3. Never answer a reviewer's questions with planning narrative. When it asks for facts, direct it
   to repository evidence (files, the plan contract).
4. When the harness cannot spawn a write-capable fresh subagent, use the **terminating manual
   fallback** (do **not** re-enter this Orchestration section from a fresh session):
   follow `.agents/skills/implementation-handoff/SKILL.md` with the plan path and suggested skill
   `implementation-design-review`. Its design-review branch owns the fallback's prompt generation,
   stop/wait, resume, and mechanical combine-only transitions. Do not start another reviewer or
   orchestration session.
5. After both reviewers report, apply **Combine data and ownership** from
   [reference.md](reference.md) to the reported verdict strings.
6. Create or completely replace the official pointer stub using the fixed path and exact shape in
   [reference.md](reference.md).
7. Report the stub path and official verdict.

## After orchestration

- **On-demand:** stop. Do not fix the plan and do not run plan-review.
- **Planning:** handle the official verdict in `implementation-planning` (fix loop, then plan-review
  on Pass). Do not re-score findings.

The artifacts are ephemeral and non-canonical. On every re-review, use the same fixed paths and
replace the prior files instead of appending history.
