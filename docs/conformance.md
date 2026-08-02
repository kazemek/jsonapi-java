# JSON:API v1.1 Conformance Checklist

Conformance is reported per feature as: **supported**, **pass-through**, **delegated**, **deferred**, or **out of scope**.

This checklist is seeded by Phase 1.1 (`jsonapi-java-core`), Phase 1.2
(`jsonapi-java-annotations`), Phase 1.3 (`jsonapi-java-core` update validation), Phase 2.1
(`jsonapi-java-jackson3` document writer), Phase 2.2 (`jsonapi-java-jackson3` domain-to-resource
write mapping), and Phase 2.4 (`jsonapi-java-jackson3` document reader), and cross-checked by
Phase 2.5 against pinned JSON:API 1.1 draft schemas. Read-side mapping and typed envelopes remain
**deferred** to their Phase 2 milestones.

## Document structure (Phase 1.1 — supported)

| Rule                                                                        | Status       | Notes                                                                                                                                                                                                |
|-----------------------------------------------------------------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Top-level members: `data`, `errors`, `meta`, `jsonapi`, `links`, `included` | supported    | `JsonApiDocument`                                                                                                                                                                                    |
| At least one of `data`, `errors`, `meta`, or extension member               | supported    | Local construction; `@` members alone are insufficient                                                                                                                                               |
| `data` and `errors` must not coexist                                        | supported    | Local construction                                                                                                                                                                                   |
| `included` absent when `data` absent                                        | supported    | Local construction                                                                                                                                                                                   |
| Explicit `"data": null` vs absent `data`                                    | supported    | `DocumentData.NullData` vs Java `null`                                                                                                                                                               |
| Primary data: resource, collection, identifier, identifier collection       | supported    | Sealed `DocumentData`                                                                                                                                                                                |
| Resource `type` required                                                    | supported    | `ResourceObject`, `ResourceIdentifier`                                                                                                                                                               |
| Resource `id` required except create context                                | supported    | Aggregate validator; empty/whitespace strings are present                                                                                                                                            |
| Local identifier (`lid`) for new resources                                  | supported    | `ResourceIdentifier`; structured `ResourceIdentity` keys                                                                                                                                             |
| Attributes / relationships flat wrappers                                    | supported    | Semantic maps reject `type`/`id`, `@`, and extension names                                                                                                                                           |
| Absent vs present-empty attributes/relationships                            | supported    | `null` wrapper vs `empty()`                                                                                                                                                                          |
| `@` and extension members in containers                                     | pass-through | Only via `additionalMembers`; namespace policy in aggregate                                                                                                                                          |
| Relationship linkage: absent, null, single, collection                      | supported    | `RelationshipData` sealed variants                                                                                                                                                                   |
| Link-only and meta-only relationships                                       | supported    | `Relationship`                                                                                                                                                                                       |
| Links: string and object forms                                              | supported    | Sealed `Link`; object form preserves additional members                                                                                                                                              |
| `hreflang` canonical list representation                                    | supported    | `Link.ObjectLink`; writer always emits JSON array form                                                                                                                                               |
| Nullable pagination links                                                   | supported    | `Links` null-preserving map                                                                                                                                                                          |
| Meta flat object (no synthetic `members` key)                               | supported    | `Meta`                                                                                                                                                                                               |
| Error object requires ≥1 standard member                                    | supported    | `ErrorObject`                                                                                                                                                                                        |
| Error source additional members                                             | pass-through | `ErrorSource.additionalMembers`                                                                                                                                                                      |
| Additional member name grammar                                              | supported    | `MemberNames` (alphanumeric namespaces, may start with digit)                                                                                                                                        |
| Reserved dedicated members in additional maps                               | supported    | Document, resource, identifier, relationship, error, jsonapi, link, source                                                                                                                           |
| Non-empty relationships                                                     | supported    | At least one of `data`, `links`, `meta`, or extension                                                                                                                                                |
| Links-only relationship minimum                                             | supported    | Non-pagination link locally; aggregate requires self/related/allowed ext/profile                                                                                                                     |
| Parameterized link media types                                              | supported    | RFC 7231 OWS (SP/HTAB) only immediately before `;`; leading/terminal bare-type and final-parameter trailing whitespace rejected; tokens; obs-text `%x80-FF`; HTAB qdtext; quoted-pair restricts CTLs |
| Link relation syntax                                                        | supported    | LOALPHA registered type or absolute URI; semantic keys use relation grammar                                                                                                                          |
| Absolute extension/profile URIs                                             | supported    | ASCII RFC 3986 absolute URI; structured authority (userinfo/host/port, IP-literal)                                                                                                                   |
| Unknown unnamespaced members rejected                                       | supported    | Aggregate validator; profile members allowed via context                                                                                                                                             |
| Extension namespace policy                                                  | supported    | `ValidationContext.allowedExtensionNamespaces`; includes link-object `meta`                                                                                                                          |
| Profile member policy                                                       | supported    | Additional members and policy-permitted link relations                                                                                                                                               |
| Profile URI policy                                                          | supported    | Enforced when `allowedProfileUris` is non-empty                                                                                                                                                      |
| Duplicate resource identities                                               | supported    | `DUPLICATE_RESOURCE_IDENTITY`; structured identity; provisional lid aliases; per-array uniqueness for primary/relationship identifier collections                                                    |
| Full linkage                                                                | supported    | Canonical alias store/lookup; sparse-fieldset exception                                                                                                                                              |
| Consistent local identifiers                                                | supported    | Order-independent one-to-one `id`↔`lid` partners                                                                                                                                                     |
| Context-specific link members                                               | supported    | `LinksContext`; `@` keys only via additional members                                                                                                                                                 |
| Relationship pagination cardinality                                         | supported    | Derived from linkage; occurrence-keyed tri-state hints (`TO_ONE`/`TO_MANY`) for link-only                                                                                                            |
| Top-level pagination requires collection                                    | supported    | Proven from `ResourceCollection` and `IdentifierCollection`                                                                                                                                          |
| Open JSON value shapes                                                      | supported    | Immutable numbers; cycles rejected with stable diagnostics                                                                                                                                           |
| Null collection payloads and elements                                       | supported    | Stable `NULL_COLLECTION_*` codes; empty lists remain valid                                                                                                                                           |
| Null required single payloads                                               | supported    | `NULL_REQUIRED_VALUE` for single resource/identifier/linkage                                                                                                                                         |
| Validation context null diagnostics                                         | supported    | `NULL_REQUIRED_VALUE` for usage, links context, policy sets/elements, hints                                                                                                                          |
| Resource type member-name grammar                                           | supported    | Only `null` is missing; Unicode-legal types accepted via `MemberNames`                                                                                                                               |
| Defensive collection copies                                                 | supported    | Model types and `ValidationContext`                                                                                                                                                                  |
| URI-reference syntax                                                        | supported    | ASCII RFC 3986; structured authority; empty string allowed; raw non-ASCII rejected                                                                                                                   |

## Resource update request validation (Phase 1.3 — supported)

| Rule                                                                                                            | Status       | Notes                                                                                        |
|-----------------------------------------------------------------------------------------------------------------|--------------|----------------------------------------------------------------------------------------------|
| Update primary data must be one resource object (absent, null, collection, or identifier primary data rejected) | supported    | `UPDATE_REQUIRES_SINGLE_RESOURCE` at `/data`                                                 |
| Update resource `id` required; lid-only rejected                                                                | supported    | Reuses `RESOURCE_ID_REQUIRED` at `/data/id`; inherited non-create identity rule              |
| Every supplied relationship must contain replacement `data`                                                     | supported    | `RELATIONSHIP_DATA_REQUIRED` at `/data/relationships/<name>/data`; primary resource only     |
| Relationship linkage preserved: null, single, empty and non-empty collection                                    | supported    | All `RelationshipData` variants valid replacements                                           |
| Omitted/present-empty attribute and relationship wrappers; explicit-null attribute values preserved             | supported    | Absent vs `Attributes.empty()` vs explicit null values; no normalization                     |
| Optional expected endpoint identity comparison                                                                  | supported    | `ENDPOINT_IDENTITY_MISMATCH` at `/data/type` or `/data/id`; supplied via `ValidationContext` |
| Update rules scoped to the primary resource                                                                     | supported    | `included` resources keep response semantics; full linkage still enforced                    |
| Command application (PATCH binding)                                                                             | deferred     | Phases 2.11 and 2.17; adapters bind, applications apply                                      |
| HTTP/route identity derivation and mutation                                                                     | out of scope | Application-owned; core compares only a supplied expected identity                           |

## Annotation metadata (Phase 1.2 — supported)

| Rule                                                         | Status    | Notes                                                                 |
|--------------------------------------------------------------|-----------|-----------------------------------------------------------------------|
| `@JsonApiResource(type)` on domain types                     | supported | Runtime-retained, `@Documented`; not `@Inherited`                     |
| `@JsonApiId` marker on logical properties                    | supported | Fields, methods, parameters, and record components                    |
| `@JsonApiAttribute(name)` optional attribute rename          | supported | Empty `name()` retains Jackson's logical property name                |
| `@JsonApiRelationship(name)` linkage metadata                | supported | Name only; no inclusion, fetch, cascade, or persistence elements      |

## Codec / wire format (Phases 2.1 and 2.4 — supported)

| Rule                                             | Status   | Notes |
|--------------------------------------------------|----------|-------|
| JSON serialization                               | supported | `jsonapi-java-jackson3` validate-then-write |
| Canonical member ordering                        | supported | Standard members in model accessor order; additional members insertion order; `hreflang` always array |
| Golden fixture write comparisons                 | supported | `fixtures/jsonapi-1.1/` |
| JSON deserialization                             | supported | Token-driven decode via public core constructors; explicit `PrimaryDataKind` |
| Malformed input diagnostics with source location | supported | `JsonApiDocumentReadException` with category, pointer, and safe location |

## Draft-schema cross-check (Phase 2.5 — supplemental)

Writer-generated fixture bytes are cross-checked against the JSON:API 1.1 **draft-PR schemas**
pinned under `fixtures/jsonapi-schema/1.1-pr1603/` (PR
[json-api/json-api#1603](https://github.com/json-api/json-api/pull/1603), fork `VGirol/json-api`
commit `4ee1c644fcc273044ecec39a6b8c0f0485abdc0e`). These are unreleased draft schemas, not an
official conformance oracle; the cross-check is **supplemental evidence only**. A schema result
never changes a feature status on this page: disagreements are resolved in favor of the textual
specification, and `JsonApiDraftSchemaSpec` keeps allow-listed fixtures failing so a schema fix
forces an intentional re-review.

| Fixture                          | Draft-schema gap                                                                      | Governing rule                                                                                                          |
|----------------------------------|---------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------|
| `member-order`                   | Draft forbids `lid` in response resources and models only `@` members, so `ext:` members are unevaluated | [v1.1 local identifiers](https://jsonapi.org/format/1.1/#document-resource-object-local-identifiers) and [extension members](https://jsonapi.org/format/1.1/#extension-members) |
| `extension-and-at-members`       | Draft models only `@` members; `ext:` members at top level and in the resource are unevaluated | [v1.1 extension members](https://jsonapi.org/format/1.1/#extension-members); PR #1603 description states @/extension rules are incomplete |
| `string-and-object-links`        | Draft `linkObject.hreflang` accepts only a string                                     | [v1.1 links](https://jsonapi.org/format/1.1/#document-links): `hreflang` is a canonical list representation; the writer always emits the array form |

`JsonApiDraftSchemaSpec` runs fully offline: the draft URI referenced by the request schemas is
mapped to the vendored response schema, all four schema files are SHA-256-pinned, every applicable
writer fixture is classified as response or create-resource document and validated against the
matching usage-specific schema, and one malformed control per schema kind (response,
create-resource, update-resource, update-relationship) proves the harness rejects invalid
documents.

## Domain mapping (Phase 2.2 — supported; Phases 2.3–2.17 — deferred)

| Rule                                                    | Status       | Notes                                                  |
|---------------------------------------------------------|--------------|--------------------------------------------------------|
| Jackson-visible domain-to-resource mapping (write-side) | supported    | Phase 2.2; produce ResourceObject from annotated types |
| Flat resource-to-DTO binding                            | deferred     | Phases 2.9 and 2.15; validated document first          |
| Typed domain document envelopes                         | deferred     | Phases 2.10 and 2.16                                   |
| Independent typed binding of `included` resources       | deferred     | Phases 2.10 and 2.16; no relationship injection        |
| Presence-aware resource-update commands                 | deferred     | Core update validation supported (Phase 1.3); command binding deferred to Phases 2.11 and 2.17 |
| Automatic domain graph hydration                        | out of scope | Linkage resolution remains application policy          |
| Automatic mutation of domain or persistence objects     | out of scope | Applications apply authorized update commands          |

## Query parameters (Phase 3.1 — delegated)

| Rule                                                  | Status    |
|-------------------------------------------------------|-----------|
| `filter`, `sort`, `page`, `fields`, `include` parsing | delegated |

## HTTP / endpoints (out of scope)

| Rule                                             | Status       | Notes                   |
|--------------------------------------------------|--------------|-------------------------|
| Spring annotated DTO and typed-envelope binding | deferred     | Phase 3.3               |
| Endpoint availability and operation semantics   | out of scope | Application-owned       |
| HTTP status selection                            | out of scope | Except adapter behavior |
| Content negotiation beyond adapter               | out of scope | Application-owned       |
