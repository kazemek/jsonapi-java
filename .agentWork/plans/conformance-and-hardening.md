# Conformance and Hardening

> **Scope:** All implemented modules  
> **Dependencies:** [Jackson 2 Document Writer](jackson2-document-writer.md), [Jackson 2 Document Reader](jackson2-document-reader.md), [Jackson 2 Domain-to-Resource Mapping](jackson2-domain-resource-mapping.md), [Jackson 2 Compound Serialization](jackson2-compound-serialization.md), [Jackson 2 Sparse Fieldsets](jackson2-sparse-fieldsets.md), [Jackson 2 Flat DTO Reader](jackson2-flat-dto-reader.md), [Jackson 2 Typed Domain Envelope](jackson2-typed-domain-envelope.md), [Jackson 2 Presence-Aware PATCH Binding](jackson2-presence-aware-patch-binding.md), [Optional Query-Parameter Parser](query-parameter-parser.md), [Spring WebMVC Adapter](spring-webmvc-adapter.md), [Spring WebMVC Flat DTO Binding](spring-webmvc-flat-dto-binding.md), [Spring WebMVC Presence-Aware PATCH Binding](spring-webmvc-patch-binding.md)  
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
