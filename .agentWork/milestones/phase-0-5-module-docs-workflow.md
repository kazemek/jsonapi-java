# Phase 0.5 — Module Docs Discovery and Maintenance

> **Scope:** Repository workflow  
> **Dependencies:** Phase 0.4  
> **Status:** Complete

## Goal

Teach agents to gather module-scoped knowledge without scanning the whole repository, and keep dual-audience module documentation current via a shared pattern and project skill.

## Deliverables

- Add a Targeted discovery section to `AGENTS.md` that routes agents through `settings.gradle.kts`, `<module>/README.md`, package-info, linked ADRs/conformance, then narrow module sources.
- Add a project `module-docs` skill that creates or updates module README, `package-info.java`, focused entry-point Javadoc, and root/AGENTS links using the `jsonapi-java-core` dual-audience pattern.
- Require the skill as a conditional completion gate when public module surface changes (packages, entry points, validate/read flows, non-goals, or agent-relevant invariants).
- Index this milestone in `.agentWork/milestones/README.md`.

## Non-goals

- Generating READMEs for modules that do not exist yet.
- Changing Sonar or Spotless completion gates.
- Moving vision, ADRs, or the conformance checklist into module trees.
- Auto-invoking the skill from ambient context (`disable-model-invocation` remains true).

## Acceptance criteria

- [x] `AGENTS.md` documents the targeted discovery order and distinguishes root planning docs from module orientation.
- [x] `AGENTS.md` requires the `module-docs` skill after implementation when public module surface changed.
- [x] `.cursor/skills/module-docs/SKILL.md` encodes the dual-audience checklist (README sections, package-info, entry-point Javadoc, root/AGENTS links, anti-duplication).
- [x] Existing `jsonapi-java-core` documentation satisfies the skill checklist without a drive-by rewrite.
- [x] `.agentWork/milestones/README.md` lists Phase 0.5 in dependency order and the milestone index.
