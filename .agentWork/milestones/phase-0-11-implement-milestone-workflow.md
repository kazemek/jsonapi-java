# Phase 0.11 — Implement-Milestone Workflow

> **Scope:** Repository workflow
> **Dependencies:** Phase 0.2, Phase 0.7, Phase 0.10
> **Status:** In progress

## Goal

Implement one milestone end-to-end: resolve the contract, read context, implement it, run the
completion gates, and verify it with a milestone review executed by a fresh-context subagent so the
review is independent of the implementing session's context and reasoning.

## Research and constraints

- [Phase 0.2](phase-0-2-milestone-review-workflow.md) established the on-demand milestone review and
  its ephemeral artifacts; the review procedure in `.agents/skills/milestone-review/SKILL.md` must
  remain the single source of truth.
- [Phase 0.7](phase-0-7-milestone-planning-workflow.md) and
  [Phase 0.10](phase-0-10-task-scoped-discovery-and-doc-pattern.md) established context-bounded
  planning and task-scoped routing; implementation context loading must follow that route and not
  duplicate module documentation.
- `AGENTS.md` still references the historical `.cursor/skills/` path; actual project skills live
  under `.agents/skills/`.

## Deliverables

- Add a project `implement-milestone` skill that implements one milestone and runs the
  `milestone-review` procedure in a fresh-context subagent with a bounded fix loop.
- Define the isolation contract: the reviewer subagent receives only the milestone path, the
  canonical artifact path derived from the milestone basename, and the instruction to follow the
  `milestone-review` skill; it derives the change set itself from git; it is never resumed or given
  implementation narrative.
- Define the milestone status lifecycle: `Not started` → `In progress` on implementation start,
  `Complete` only after a review `Pass`; the status is kept in sync between the milestone file and
  the index entry in `.agentWork/milestones/README.md`; acceptance criteria are marked `[x]` by the
  implementer as evidence and never edited by the reviewer.
- Document the workflow and isolation rationale in `AGENTS.md` and `.agentWork/milestones/README.md`,
  and correct the stale skills path.
- Keep the skill harness-independent: no harness-specific agent configuration; when the harness
  cannot spawn a write-capable fresh subagent, fall back to a contract-only handoff plus a manual
  fresh review session.

## Non-goals

- Replacing the `milestone-review` skill or its artifact format.
- Automatically invoking the workflow; the skill is explicitly invoked.
- Adding a pre-implementation planning stage; the milestone is the plan.
- Editing completed milestone contracts or adding harness-specific (`.opencode/`, `.cursor/`)
  configuration.

## Implementation boundaries

- The `implement-milestone` skill lives at `.agents/skills/implement-milestone/SKILL.md` and
  delegates review procedure to `.agents/skills/milestone-review/SKILL.md`.
- `AGENTS.md` is the canonical task router; the skill references completion-gate skills rather than
  duplicating their checklists.
- Reviews remain ephemeral artifacts under `.agentWork/.session/`, overwritten on re-review.

## Test strategy

- Check the skill's resolve gates (status and dependency), reviewer prompt template, fix-loop cap,
  and handoff fallback for consistency with `milestone-review` and the milestone lifecycle.
- Verify the fix loop re-runs all completion gates after each fix batch and before the next fresh
  review.
- Verify `AGENTS.md` routing, the corrected skills path, and the milestone index entry.
- Run the repository build, formatting, and Sonar Quality Gate completion workflows.

## Acceptance criteria

- [x] `.agents/skills/implement-milestone/SKILL.md` defines an explicitly invoked workflow (no
  automatic invocation).
- [x] The review phase spawns a new fresh-context subagent whose prompt contains only the milestone
  path, artifact path, and reference to the `milestone-review` skill; no implementation narrative is
  passed.
- [x] The reviewer derives the change set itself from git; re-reviews always use new subagents; the
  fix loop is capped at two re-reviews.
- [x] The skill defines the milestone status lifecycle and honors it during implementation and
  verdict handling.
- [x] `AGENTS.md` documents the implementation workflow, the fresh-context isolation rationale, and
  the corrected skills path (`.agents/skills/`).
- [x] `.agentWork/milestones/README.md` documents the workflow in its dependency order and index.
- [x] `./gradlew clean build` passes.
- [x] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`), and Sonar Quality
  Gate passes (or Sonar is reported blocked when `SONAR_TOKEN` is unavailable, which keeps the
  milestone `In progress` until CI confirms).
