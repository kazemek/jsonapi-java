# ADR-008: Public Namespace and Maven Group

**Status:** Accepted  
**Date:** 2026-07-26

## Context

Public Java packages and Maven coordinates must use a namespace the project owner controls. Maven Central requires a verified namespace before publication. The provisional placeholder `io.github.jsonapi` was never assumed to be owned.

The project repository is hosted at `https://github.com/kazemek/jsonapi-java`.

## Decision

Use these coordinates:

- Maven Central namespace and Gradle `group`: `io.github.kazemek`
- Java base package: `io.github.kazemek.jsonapi`
- Package suffixes under that base: `core.model`, `core.validation`, `annotation`, `jackson`, `query`, and adapter-specific Spring packages

Artifact IDs remain `jsonapi-java-*`.

## Evidence

On 2026-07-26, the Maven Central Portal showed `io.github.kazemek` as Verified for the publisher account signed in with GitHub user `kazemek`, following Sonatype’s GitHub ownership process.

## Consequences

- Public source types may be added under `io.github.kazemek.jsonapi.*`.
- A stable release publishes artifacts under group `io.github.kazemek`.
- Changing the group or base package requires a new ADR and a coordinated rename across modules, docs, and live implementation plans.
- The provisional name `io.github.jsonapi` is rejected and must not appear in source or publication metadata.
