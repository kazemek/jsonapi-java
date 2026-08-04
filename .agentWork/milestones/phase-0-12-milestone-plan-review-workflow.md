# Phase 0.12 — Milestone Plan-Review Workflow

> **Scope:** Repository workflow
> **Dependencies:** Phase 0.2, Phase 0.7, Phase 0.11
> **Status:** Complete

## Goal

Create, refine, or decompose milestones and verify each resulting contract with a milestone plan
review executed by a fresh-context subagent so the review is independent of the planning session's
context and reasoning; keep on-demand plan/spec review available.

## Research and constraints

- [Phase 0.2](phase-0-2-milestone-review-workflow.md) established on-demand implementation review
  artifacts under `.agentWork/.session/`; plan review uses a parallel ephemeral artifact naming
  pattern without replacing that skill.
- [Phase 0.7](phase-0-7-milestone-planning-workflow.md) established create/refine/decompose; planning
  previously ended when files and the index were written.
- [Phase 0.11](phase-0-11-implement-milestone-workflow.md) established the fresh-context review,
  isolation prompt, bounded fix loop, and handoff fallback that this phase mirrors for planning.
- Project skills live under `.agents/skills/`; `AGENTS.md` is the canonical task router.

## Deliverables

- Add a project `milestone-plan-review` skill that reviews one milestone contract against planning
  rules and repository evidence, writes
  `.agentWork/.session/milestone-plan-review-<milestone-basename>.md`, and does not edit milestones
  or implement the planned feature.
- Extend `milestone-planning` so that after synchronize-and-verify it runs `milestone-plan-review`
  in a fresh-context subagent per created or refined milestone, with a bounded fix loop capped at
  two re-reviews; overall Pass only when every such milestone Passes.
- Define the isolation contract: the reviewer subagent receives only the milestone path, the
  canonical artifact path derived from the milestone basename, and the instruction to follow the
  `milestone-plan-review` skill; it is never resumed or given planning narrative.
- Document dual on-demand and delegated routing for plan/spec vs implementation review in
  `AGENTS.md` and `.agentWork/milestones/README.md`.
- Keep the skill harness-independent: no harness-specific agent configuration; when the harness
  cannot spawn a write-capable fresh subagent, fall back to a contract-only handoff plus a manual
  fresh review session.

## Non-goals

- Replacing the `milestone-review` or `implement-milestone` skills or their artifact formats.
- Automatically invoking the workflow; skills remain explicitly invoked.
- Changing feature-milestone contracts or adding harness-specific (`.opencode/`, `.cursor/`)
  configuration.
- Inlining the personal `handoff` skill into the repository.

## Implementation boundaries

- The `milestone-plan-review` skill lives at `.agents/skills/milestone-plan-review/SKILL.md`.
- The `milestone-planning` skill lives at `.agents/skills/milestone-planning/SKILL.md` and delegates
  review procedure to `milestone-plan-review`.
- `AGENTS.md` is the canonical task router; plan review evaluates planning rules rather than
  scoring an implementation.
- Plan-review artifacts remain ephemeral under `.agentWork/.session/`, overwritten on re-review.

## Test strategy

- Check the plan-review skill's resolve gates, severity model, contract-coverage dimensions, and
  artifact template against planning rules in `milestone-planning`.
- Check the planning skill's verbatim reviewer prompt, isolation rules, fix-loop cap, and handoff
  fallback for symmetry with `implement-milestone`.
- Verify `AGENTS.md` routing and the milestone index entry for this phase.
- Docs/skills-only change scope: no repository build, Spotless, or Sonar gates required.

## Acceptance criteria

- [x] `.agents/skills/milestone-plan-review/SKILL.md` defines an explicitly invoked plan/spec review
  workflow that does not mutate milestone contracts.
- [x] `.agents/skills/milestone-planning/SKILL.md` requires a fresh-context plan review after
  synchronize-and-verify; the reviewer prompt contains only the milestone path, artifact path, and
  reference to `milestone-plan-review`; no planning narrative is passed.
- [x] Re-reviews always use new subagents; the fix loop is capped at two re-reviews; overall Pass
  requires every created or refined milestone in the run to Pass.
- [x] `AGENTS.md` documents plan/refine routing through the plan-review loop and on-demand
  plan/spec review.
- [x] `.agentWork/milestones/README.md` documents the planning review loop and distinguishes plan vs
  implementation review artifact paths in its dependency order and index.
