# WebFlux Adapter Evaluation

> **Candidate module:** `jsonapi-java-spring-webflux`  
> **Dependencies:** [Spring WebMVC Adapter](spring-webmvc-adapter.md), [Spring WebMVC Flat DTO Binding](spring-webmvc-flat-dto-binding.md), [Spring WebMVC Presence-Aware PATCH Binding](spring-webmvc-patch-binding.md)  
> **Status:** Not started
> **Work item:** KAZ-39

## Goal

Decide whether a WebFlux adapter can provide the same document and flat DTO contract without
coupling core or WebMVC code to reactive APIs.

## Evaluation

- Map WebMVC media-type, codec, typed envelope, DTO, PATCH command, query argument, and
  exception-handler behavior to WebFlux extension points.
- Verify streaming constraints against whole-document validation, DTO binding, and compound
  document deduplication.
- Define buffering and input-size limits explicitly.
- Confirm content negotiation and error behavior can share framework-neutral policy code.
- Measure dependency, testing, release, and maintenance cost.

## Outcomes

The plan ends with either:

- accepted size-gated implementation plans with document and DTO parity criteria; or
- a documented decision to defer WebFlux.

No WebFlux types enter core, annotations, Jackson, query, or WebMVC modules.

## Acceptance criteria

- [ ] A written go/defer decision records document, DTO, typed-envelope, PATCH, and media-type
      parity plus maintenance cost.
- [ ] Streaming claims are not made where aggregate validation or domain binding requires
      buffering.
- [ ] Shared policy is separated from framework-specific integration.
- [ ] No existing module acquires a reactive dependency.
