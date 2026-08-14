# Outlook

Outlook is tentative, revisable future direction. It is planning input, not a contract, not
current architecture, and **not a dependency**. Authority and owner/reference rules are in
[`AGENTS.md`](../../AGENTS.md). Stable product boundaries live in [`docs/vision.md`](../vision.md).

This migration adds only the Outlook documents justified here. Future planning may split Outlook
by meaningful future product area when the content warrants it. Do not add speculative Outlook
files, and do not put implementation-ready contracts, type names, or acceptance criteria in
Outlook.

Do not treat Outlook as an implementation plan. Live executable work, if any, remains under
[`.agentWork/plans/`](../../.agentWork/plans/) as temporary implementation contracts. Outlook
never overrides Snapshot, Vision, or accepted ADRs.

## Maintenance

Every Outlook document must include:

- **Uncertainty and open questions** — what is not yet decided.
- **Revisit trigger** — what event would make this current, obsolete, or need a rewrite.

When future direction becomes current engineering truth, move durable engineering facts to their
Snapshot owner (module README, package/API documentation, accepted ADR, conformance, or root
registry as appropriate). If the change also alters stable product direction or principles, update
Vision separately. Then **delete or rewrite** the Outlook so it does not keep superseded
speculation in normal discovery. When direction is abandoned, delete the Outlook; do not retain
it as a historical archive. Git remains forensic history.

Conceptual breakdown in Outlook (for example Spring transport vs DTO vs PATCH vs WebFlux) does
not by itself create child implementation plans.

## Index

- [Spring adapters](spring.md) — optional WebMVC integration and a later WebFlux evaluation.

The sections below cover remaining high-level directions that are not Spring-specific.

## Jackson 2 parity

**Tentative direction:** a separately compiled `jsonapi-java-jackson2` artifact with the same
stable conceptual contracts as Jackson 3, consuming Jackson-major-neutral types from
`jsonapi-java-jackson-common` ([ADR-007](../adr/007-module-boundaries.md)). Jackson 2 and
Jackson 3 public APIs do not share one runtime artifact.

**Uncertainty / open questions:** supported Jackson 2 minor line and compatibility claims before
a stable release; whether every Jackson 3 mapping/PATCH capability lands in lockstep.

**Revisit trigger:** when a Jackson 2 artifact is registered in the build and its module README
exists; rewrite or delete this section so current capability lives on that Snapshot surface.

Live executable Jackson 2 plans, if any, remain under `.agentWork/plans/`.

## Query-parameter parser

**Tentative direction:** an optional `jsonapi-java-query` module that parses JSON:API query
parameters into immutable, framework-neutral values without executing filtering, sorting,
pagination, sparse fieldsets, or inclusion ([ADR-001](../adr/001-product-boundary.md),
[Vision](../vision.md)).

**Uncertainty / open questions:** which optional query families ship in the first parser surface;
how strictly unsupported features are rejected versus left to the application.

**Revisit trigger:** when `jsonapi-java-query` exists as a documented module; move current
behavior to that module README and conformance, then rewrite or delete this section.

## Production readiness

**Tentative direction:** before a stable release, complete conformance and malformed-input
coverage, define size/depth/traversal limits, establish performance baselines and compatibility
policy, and decide JPMS support. Publication stays under the verified namespace
([ADR-008](../adr/008-public-namespace.md)).

**Uncertainty / open questions:** which limits are library-enforced versus application policy;
JPMS vs automatic-module-name; supported Java, Jackson, and Spring version ranges.

**Revisit trigger:** when hardening or publication work starts as an execution plan, or when a
stable release policy is accepted as an ADR or module/docs contract; do not keep this section as
a substitute for those owners.
