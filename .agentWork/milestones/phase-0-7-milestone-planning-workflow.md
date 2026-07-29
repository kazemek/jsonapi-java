# Phase 0.7 — Milestone Planning Workflow

> **Scope:** Repository workflow  
> **Dependencies:** Phase 0.6  
> **Status:** Complete

## Goal

Define an explicitly invoked, research-backed workflow that creates, refines, or decomposes implementation milestones into context-bounded delivery contracts.

## Deliverables

- Add a project `milestone-planning` skill that writes permanent milestone files under `.agentWork/milestones/`.
- Require targeted repository exploration, relevant authoritative research, vision alignment, dependency analysis, and ADR identification before writing a milestone.
- Define lifecycle rules for creating and refining milestones without rewriting milestones whose implementation has started.
- Define a mandatory size gate and decompose oversized work into independently implementable, ordered milestones.
- Document the workflow and sizing expectation in `AGENTS.md`, and keep `.agentWork/milestones/README.md` dependency order and milestone index synchronized.

## Sizing contract

An implementable milestone describes one coherent, independently testable outcome that a coding agent can deliver in one focused task and commit without broad repository discovery. It normally has one primary module or layer, one principal capability, a narrow set of relevant packages and tests, no more than five deliverables, and no more than eight acceptance criteria.

The planning workflow must split work when independent capabilities, modules, architectural decisions, or verification surfaces can be delivered separately. Integration may cross modules only when the integration itself is the single outcome. Decomposed milestones must have explicit dependencies and independently verifiable acceptance criteria; an oversized umbrella milestone must not remain as an implementable contract.

## Non-goals

- Automatically invoking milestone planning for every feature request.
- Replacing the vision, ADRs, module documentation, or conformance documents with milestone prose.
- Estimating milestone size from markdown line count alone.
- Modifying source code or implementing the milestone being planned.
- Rewriting completed or implementation-started milestones instead of creating follow-up work.

## Acceptance criteria

- [x] `.cursor/skills/milestone-planning/SKILL.md` defines an explicitly invoked creation, refinement, and decomposition workflow.
- [x] The workflow performs targeted exploration and records only implementation-relevant research constraints and sources.
- [x] The workflow verifies vision, dependency, lifecycle, and ADR implications before writing files.
- [x] The skill writes actual milestone files under `.agentWork/milestones/` and synchronizes dependency order and index; repository guidance documents the workflow.
- [x] The size gate requires one focused implementation context and applies the stated deliverable and acceptance-criteria bounds.
- [x] Oversized work is emitted as ordered, independently testable milestone files rather than one implementable umbrella milestone.
- [x] Completed or implementation-started milestones remain historical contracts; refinements become follow-up milestones.
- [x] `./gradlew clean build` passes, Spotless passes (`spotlessApply` then `spotlessCheck`), and Sonar Quality Gate passes (or Sonar is reported blocked when `SONAR_TOKEN` is unavailable).
