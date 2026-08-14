# Outlook: Spring adapters

**Status:** tentative. This is not an implementation plan and does not satisfy dependencies.

Optional Spring integration is in-product ([Vision](../vision.md),
[ADR-001](../adr/001-product-boundary.md), [ADR-007](../adr/007-module-boundaries.md)). Current
capability is none: `jsonapi-java-spring-webmvc` is not in the build. Executable Spring work, if
any, remains the existing milestone files under
[`.agentWork/milestones/`](../../.agentWork/milestones/README.md).

## Direction

A thin Spring Boot **WebMVC** adapter should integrate the JSON:API media type without taking
ownership of endpoints, persistence, authorization, or query execution:

- negotiate `application/vnd.api+json` without replacing ordinary JSON handling;
- read and write validated core documents;
- expose parsed query arguments without executing them;
- render codec, validation, query, and media-type failures as JSON:API errors with safe defaults
  (unregistered application exceptions are not leaked).

Later WebMVC increments, if pursued, are DTO and typed-envelope controller binding, then
presence-aware PATCH command arguments. Applications authorize and apply those commands; the
adapter does not mutate domain or persistence objects.

**WebFlux** is a later **evaluation**, not a committed product surface. A reactive adapter would
be a separate artifact. No WebFlux or reactive types belong in core, annotations, Jackson, query,
or WebMVC modules. The first WebMVC adapter targets Jackson 3; Jackson 2 remains usable without
Spring integration, and a Jackson 2 WebMVC adapter is not implied.

Conceptual parts (transport, DTO binding, PATCH arguments, WebFlux evaluation) describe
direction. They do not by themselves require separate execution plans.

## Uncertainty and open questions

- Supported Spring Boot / Spring Framework line.
- Whether WebFlux is implemented, deferred, or declined after evaluation.
- How much query-family coverage the first WebMVC adapter exposes versus leaving to the
  application.
- Whether a Jackson 2 WebMVC adapter is ever in scope.

## Revisit trigger

Rewrite or delete this Outlook when any of the following happen:

- WebMVC adapter implementation starts and a module README exists (current capability moves to
  that Snapshot owner).
- WebMVC DTO and PATCH behavior is stable enough to decide WebFlux (record a go/defer decision
  in an ADR or module docs; do not keep a superseded evaluation here).
- The product drops Spring adapters from intended scope (Vision / ADR-007 change; delete this
  document).
