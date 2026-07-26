# Phase 0.2 — Milestone Review Workflow

> **Scope:** Repository workflow  
> **Dependencies:** Phase 0.1  
> **Status:** Complete

## Goal

Define an on-demand, agent-driven code review workflow that evaluates an implementation against its milestone contract and records an ephemeral review artifact.

## Deliverables

- Add a project `milestone-review` skill for reviewing implementation changes against a selected milestone.
- Define a stable review format with evidence-backed findings, acceptance-criteria coverage, test evidence, and a verdict.
- Document the on-demand review workflow in `AGENTS.md` and the milestone lifecycle documentation.
- Store generated reviews under `.agentWork/.session/` and exclude that directory from version control.
- Overwrite the current review artifact when the same milestone is reviewed again.

## Non-goals

- Automatically reviewing every implementation.
- Replacing milestones, ADRs, or the vision with session review artifacts.
- Migrating unrelated agent rules or slash commands.
- Retaining review history in version control.

## Acceptance criteria

- [x] `.cursor/skills/milestone-review/SKILL.md` defines an explicitly invoked milestone review workflow.
- [x] A review is based on one milestone under `.agentWork/milestones/`.
- [x] Review artifacts use `.agentWork/.session/milestone-review-<milestone-basename>.md`.
- [x] Re-reviewing a milestone overwrites its existing review artifact.
- [x] Reviews report prioritized findings with file and line evidence, milestone acceptance-criteria coverage, relevant test/build evidence, and a verdict.
- [x] `AGENTS.md` and `.agentWork/milestones/README.md` document the workflow and distinguish ephemeral reviews from permanent milestones.
- [x] `.agentWork/.session/` is ignored by Git.
- [x] `./gradlew clean build` passes.
