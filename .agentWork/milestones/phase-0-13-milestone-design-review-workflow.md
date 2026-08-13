# Phase 0.13 — Milestone Design-Review Workflow

> **Scope:** Repository workflow
> **Dependencies:** Phase 0.7, Phase 0.12
> **Status:** Complete

## Goal

Verify each created or refined milestone with a two-reviewer design review before plan-review, so
the technical approach is judged independently of the planning session and independently of
contract completeness.

## Research and constraints

- [Phase 0.12](phase-0-12-milestone-plan-review-workflow.md) established plan-review as a
  completeness and implementability check; it can Pass a contract whose technical approach is
  wrong.
- [Phase 0.7](phase-0-7-milestone-planning-workflow.md) established create/refine/decompose.
- Architecture and API/semantics are not independent in this repository (ADR-001, ADR-002, ADR-003,
  ADR-007, ADR-009, ADR-010); the second reviewer is adversarial only.
- Project skills live under `.agents/skills/`; `AGENTS.md` is the canonical task router.
- Docs/skills-only change scope: no repository build, Spotless, or Sonar gates required.

## Deliverables

- Add a project `milestone-design-review` skill that always spawns Design and Adversarial reviewers
  in parallel, combines reported verdicts with true worst-wins, writes a pointer stub at
  `.agentWork/.session/milestone-design-review-<milestone-basename>.md`, and does not edit
  milestones or implement the planned feature.
- Extend `milestone-planning` so that after synchronize-and-verify it follows that orchestration
  per created or refined milestone, with a bounded fix loop capped at two re-reviews, then runs
  plan-review only after design Pass; overall Pass requires both.
- Keep plan-review from recommending alternative designs; ADR identification remains a planning
  finding.
- Document on-demand and delegated routing in `AGENTS.md` and `.agentWork/milestones/README.md`.
- Keep the skill harness-independent; when a write-capable fresh subagent cannot be spawned, fall
  back to `milestone-handoff` with review-kind `design-review`.

## Non-goals

- A 1-vs-3 panel, risk classifier, escalation path, synthesizer agent, or `Redesign required`
  verdict.
- Replacing `milestone-plan-review`, `milestone-review`, or `implement-milestone`.
- Automatically invoking the workflow; skills remain explicitly invoked.
- Gating `implement-milestone` on gitignored design-review artifacts.
- Re-triggering design review after plan-review edits in the same planning run.
- Changing feature-milestone contracts or adding harness-specific configuration.

## Implementation boundaries

- The skill lives at `.agents/skills/milestone-design-review/`; orchestration is only in
  `SKILL.md`; reviewers read `design.md` or `adversarial.md` only.
- `milestone-planning` follows that Orchestration section and must not fork spawn/combine text.
- Blocking bars are citation-gated in the reviewer procedures; the orchestrator trusts verdict
  strings.
- Design-review artifacts remain ephemeral under `.agentWork/.session/`, overwritten on re-review.

## Test strategy

- Cross-read the four skills for the responsibility split.
- Confirm two-reviewer independence, citation-gated `Blocks`, prerequisite-only `Blocked`, pointer
  stub with no Summary, true worst-wins, one-way pipeline, and handoff routing.
- Docs/skills-only: no repository build, Spotless, or Sonar gates.

## Acceptance criteria

- [x] `.agents/skills/milestone-design-review/SKILL.md` defines explicitly invoked orchestration
      that always runs Design and Adversarial reviewers and does not mutate milestone contracts.
- [x] `.agents/skills/milestone-planning/SKILL.md` requires design-review orchestration after
      synchronize-and-verify, then plan-review only after design Pass; overall Pass requires both;
      plan-review edits do not restart design review in the same run.
- [x] Reviewer prompts contain only the milestone path, artifact path, and procedure file; blocking
      requires a repository citation; combination is worst-wins on verdict strings.
- [x] `milestone-plan-review` does not recommend alternative designs; `milestone-handoff` supports
      `milestone-design-review`.
- [x] `AGENTS.md` and `.agentWork/milestones/README.md` document the one-way planning loop and the
      three ephemeral review kinds.
