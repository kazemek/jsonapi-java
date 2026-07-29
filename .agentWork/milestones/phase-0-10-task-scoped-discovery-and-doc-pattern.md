# Phase 0.10 — Task-Scoped Discovery and Documentation Pattern

> **Scope:** Repository workflow and agent guidance  
> **Dependencies:** Phase 0.2, Phase 0.5, Phase 0.7  
> **Status:** Complete

## Goal

Give agents one task-scoped discovery router and one reusable module-documentation pattern so they
load enough context to plan, implement, and review changes without scanning unrelated repository
areas.

## Research and constraints

- [`AGENTS.md`](../../AGENTS.md) currently separates module-targeted discovery from the mandatory
  implementation workflow, leaving their relative order ambiguous.
- [Phase 0.5](phase-0-5-module-docs-workflow.md) established thin dual-audience module
  documentation and is a completed historical contract.
- [Phase 0.7](phase-0-7-milestone-planning-workflow.md) established context-bounded milestone
  planning and is a completed historical contract.
- [`jsonapi-java-core/README.md`](../../jsonapi-java-core/README.md) and its production
  `package-info.java` files remain the golden module-documentation example.

## Deliverables

- A canonical task-scoped discovery router in `AGENTS.md` for planning, implementation, review, and
  repository-wide work, with explicit scope-expansion rules.
- A milestone index that exposes scope/module and status without opening every milestone, plus
  planning guidance that keeps the index synchronized.
- A tighter `module-docs` pattern for compact README and `package-info.java` content, link-out
  behavior, and self-verification against the golden example.
- Conditional `module-docs` hooks in milestone planning and review without duplicating the
  authoring checklist.

## Non-goals

- Automated documentation linting or a new Gradle documentation task.
- Rewriting completed Phase 0.5 or Phase 0.7 milestone contracts.
- Duplicating vision, ADR, conformance, build, or CI prose in module documentation.
- Requiring the full project vision for a narrow implementation already governed by a milestone
  and module documentation.

## Implementation boundaries

- `AGENTS.md` is the canonical task router; skills own their detailed workflows.
- `module-docs` is the canonical module-documentation checklist; planning schedules it and review
  verifies it only when public module surface changes.
- The root README remains the module registry. `AGENTS.md` routes generically through
  `<module>/README.md` rather than accumulating one link per module.
- Repository discovery starts in the affected module and broadens only when direct evidence
  requires cross-module or repository-wide context.

## Test strategy

- Check all router branches, skill triggers, links, and milestone metadata for consistency.
- Verify the existing core README and package documentation still satisfy the tightened pattern.
- Run the repository build, formatting, and Sonar Quality Gate completion workflows.

## Acceptance criteria

- [x] `AGENTS.md` has unambiguous planning, implementation, review, and repository-wide discovery
  routes plus an evidence-based scope-expansion rule.
- [x] Narrow implementation reads the governing milestone and affected module documentation;
  full-vision reading is reserved for planning or changes that can affect project direction.
- [x] Missing-milestone and historical-milestone behavior is explicit and does not silently invoke
  milestone creation.
- [x] The milestone index shows scope/module and status, and `milestone-planning` keeps that format
  synchronized.
- [x] `module-docs` defines the compact dual-audience, link-out pattern and requires a golden-example
  self-check without per-module links accumulating in `AGENTS.md`.
- [x] Milestone planning schedules, and milestone review verifies, the canonical `module-docs`
  checklist when public module surface changes.
- [x] `./gradlew clean build` passes.
- [x] Spotless and the Sonar Quality Gate pass; if `SONAR_TOKEN` is unavailable, Sonar is reported
  blocked and CI must still pass the gate.
