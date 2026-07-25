# Phase 4.1 — Conformance and Hardening

> **Scope:** All implemented modules  
> **Dependencies:** Phases 1–3.2  
> **Status:** Not started

## Goal

Turn implemented behavior into a verifiable compliance and security contract before a stable release.

## Deliverables

- A JSON:API v1.1 conformance checklist marking every relevant rule as supported, pass-through, delegated, deferred, or out of scope.
- Traceability from supported rules to positive and negative fixtures.
- Fuzz and malformed-input coverage for documents and query syntax.
- Default and configurable limits for input size, nesting, include depth, included-resource count, and open member containers.
- Threat analysis for traversal, lazy loading, information disclosure, error leakage, and resource exhaustion.
- Performance baselines for core validation, codec reads/writes, mapping metadata warm/cold paths, and compound serialization.
- User documentation for safe inclusion policies and extension/profile handling.

## Acceptance criteria

- [ ] No unqualified “JSON:API compliant” claim remains.
- [ ] Every guaranteed conformance item has an executable test reference.
- [ ] Limits fail with stable diagnostics rather than uncontrolled memory or stack failure.
- [ ] Security-sensitive defaults are documented and tested.
- [ ] Benchmarks publish environment and methodology and are regression baselines, not marketing claims.
- [ ] `./gradlew clean build` passes.
