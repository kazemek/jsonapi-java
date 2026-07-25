# Phase 0.1 — Public Namespace Decision

> **Scope:** All modules  
> **Dependencies:** None  
> **Status:** Complete

## Goal

Choose and verify the package and Maven group namespace before any public source API is implemented.

## Deliverables

- Identify a namespace controlled by the project owner, normally `io.github.<github-owner>` or a reversed owned domain.
- Verify that namespace through Maven Central's supported ownership process.
- Record the package base and Maven group in a new ADR.
- Update `AGENTS.md`, milestones, Gradle publication metadata, and test package placeholders consistently.
- Treat existing `io.github.jsonapi` references as provisional and do not assume ownership.

## Decision

- Maven group: `io.github.kazemek`
- Java base package: `io.github.kazemek.jsonapi`
- Recorded in [ADR-008](../../docs/adr/008-public-namespace.md)

## Acceptance criteria

- [x] Namespace ownership is evidenced and documented.
- [x] One base package and Maven group are used consistently.
- [x] No source implementation milestone remains blocked on package naming.
- [x] `./gradlew clean build` passes after package updates.
