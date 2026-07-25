# Phase 2.4 — Document-First Reads

> **Module:** `jsonapi-java-jackson`  
> **Dependencies:** Phases 1.1 and 2.1  
> **Status:** Not started

## Goal

Complete the supported read contract: JSON:API request and response JSON becomes a validated document model, not an automatically hydrated domain graph.

## Deliverables

- Public read APIs for bytes, streams, strings, and Jackson parsers.
- Explicit document-usage context where rules differ for create, update, relationship, and response documents.
- Resource and relationship access helpers that do not fabricate unresolved resources.
- Strict unknown-member handling with configured extension/profile pass-through.
- Resource limits for nesting depth, collection size, string size, and total input size where the host API exposes them.
- Diagnostics preserving rule code, JSON Pointer-like path, and Jackson source location.

## Behavioral rules

- Resource linkage remains identifiers unless a caller explicitly resolves it.
- Included resources remain a collection in the document; they are not injected into relationship fields.
- Local identifiers are validated within the document.
- Missing attributes and relationships remain absent, which is essential for partial update documents.
- Invalid input never produces a value advertised as a valid document.

## Non-goals

- Constructing arbitrary annotated domain values.
- Identity maps for application entities.
- Applying PATCH operations to application state.
- Fetching unresolved relationship targets.

## Acceptance criteria

- [ ] Official create, update, relationship, fetch, and error document fixtures decode into the correct model states.
- [ ] Missing members remain distinguishable from explicit null and empty values.
- [ ] Included resources are not automatically wired into domain graphs.
- [ ] Context-specific invalid documents produce stable diagnostics.
- [ ] Resource limits have deterministic failures and tests.
- [ ] `./gradlew :jsonapi-java-jackson:test` passes.
