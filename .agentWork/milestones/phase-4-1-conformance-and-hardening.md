# Phase 4.1 — Conformance and Hardening

> **Scope:** All implemented modules  
> **Dependencies:** Phases 2.15, 2.16–2.23, and 3.1–3.4  
> **Status:** Not started
> **Work item:** KAZ-30

## Goal

Turn implemented behavior into a verifiable compliance and security contract before a stable release.

## Deliverables

- A JSON:API v1.1 conformance checklist with fixture traceability for every supported document,
  mapping, typed-envelope, PATCH, query, and WebMVC rule.
- Fuzz/malformed coverage and configurable limits for input size, nesting, open containers,
  include depth/count, DTO/envelope binding, and PATCH change count.
- Threat analysis for traversal, lazy loading, included-type registration, information disclosure,
  PATCH over-posting, error leakage, and resource exhaustion.
- Reproducible performance baselines for core validation, both Jackson codecs, mapping metadata,
  compound serialization, flat DTO/envelope reads, and PATCH binding.
- User documentation for safe inclusion/fieldset/type-registration/update policies,
  extension/profile handling, and the application-owned mutation boundary.

## Acceptance criteria

- [ ] No unqualified “JSON:API compliant” claim remains.
- [ ] Every guaranteed conformance item has an executable test reference.
- [ ] Limits fail with stable diagnostics rather than uncontrolled memory or stack failure.
- [ ] Security-sensitive defaults, including included registration and PATCH field authorization,
      are documented and tested independently for both Jackson major artifacts; the supported
      Jackson 3 WebMVC integration has adapter-specific tests for those defaults, and no Jackson 2
      WebMVC support is implied.
- [ ] Benchmarks publish environment and methodology and are regression baselines, not marketing
      claims.
- [ ] `./gradlew clean build` passes.
- [ ] Spotless passes (`./gradlew spotlessApply` then `./gradlew spotlessCheck`).
- [ ] When `SONAR_TOKEN` is available, the Sonar Quality Gate passes; without it, local Sonar
      validation is explicitly blocked rather than counted as passed, and CI must still run and
      pass the gate.
