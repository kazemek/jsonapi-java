# Phase 3.3 — WebFlux Adapter Evaluation

> **Candidate module:** `jsonapi-java-spring-webflux`  
> **Dependencies:** Stable Phase 3.2 behavior  
> **Status:** Not started

## Goal

Decide whether a WebFlux adapter can provide the same JSON:API contract without coupling core or WebMVC code to reactive APIs.

## Evaluation

- Map WebMVC behavior to WebFlux codec, argument resolver, and exception-handler extension points.
- Verify streaming constraints against whole-document validation and compound-document deduplication.
- Define buffering and input-size limits explicitly.
- Confirm content negotiation and error behavior can share framework-neutral policy code.
- Measure dependency and maintenance cost.

## Outcomes

The milestone ends with either:

- an accepted implementation milestone with parity criteria; or
- a documented decision to defer WebFlux.

No WebFlux types enter core, annotations, Jackson, query, or WebMVC modules.

## Acceptance criteria

- [ ] A written go/defer decision records API parity and maintenance cost.
- [ ] Streaming claims are not made where aggregate validation requires buffering.
- [ ] Shared policy is separated from framework-specific integration.
- [ ] No existing module acquires a reactive dependency.
