# JSON:API v1.1 Conformance Checklist

Conformance is reported per feature as: **supported**, **pass-through**, **delegated**, **deferred**, or **out of scope**.

This checklist is seeded by Phase 1.1 (`jsonapi-java-core`) and Phase 1.2 (`jsonapi-java-annotations`). Wire-format round-trips are **deferred** to Phase 2.1; Jackson domain mapping remains **deferred** to Phase 2.2.

## Document structure (Phase 1.1 — supported)

| Rule                                                                        | Status       | Notes                                                       |
|-----------------------------------------------------------------------------|--------------|-------------------------------------------------------------|
| Top-level members: `data`, `errors`, `meta`, `jsonapi`, `links`, `included` | supported    | `JsonApiDocument`                                           |
| At least one of `data`, `errors`, `meta`, or extension member               | supported    | Local construction; `@` members alone are insufficient      |
| `data` and `errors` must not coexist                                        | supported    | Local construction                                          |
| `included` absent when `data` absent                                        | supported    | Local construction                                          |
| Explicit `"data": null` vs absent `data`                                    | supported    | `DocumentData.NullData` vs Java `null`                      |
| Primary data: resource, collection, identifier, identifier collection       | supported    | Sealed `DocumentData`                                       |
| Resource `type` required                                                    | supported    | `ResourceObject`, `ResourceIdentifier`                      |
| Resource `id` required except create context                                | supported    | Aggregate validator; empty/whitespace strings are present   |
| Local identifier (`lid`) for new resources                                  | supported    | `ResourceIdentifier`; structured `ResourceIdentity` keys    |
| Attributes / relationships flat wrappers                                    | supported    | Semantic maps reject `type`/`id`, `@`, and extension names  |
| Absent vs present-empty attributes/relationships                            | supported    | `null` wrapper vs `empty()`                                 |
| `@` and extension members in containers                                     | pass-through | Only via `additionalMembers`; namespace policy in aggregate  |
| Relationship linkage: absent, null, single, collection                      | supported    | `RelationshipData` sealed variants                          |
| Link-only and meta-only relationships                                       | supported    | `Relationship`                                              |
| Links: string and object forms                                              | supported    | Sealed `Link`; object form preserves additional members     |
| `hreflang` canonical list representation                                    | supported    | `Link.ObjectLink`; codec emission deferred                  |
| Nullable pagination links                                                   | supported    | `Links` null-preserving map                                 |
| Meta flat object (no synthetic `members` key)                               | supported    | `Meta`                                                      |
| Error object requires ≥1 standard member                                    | supported    | `ErrorObject`                                               |
| Error source additional members                                             | pass-through | `ErrorSource.additionalMembers`                             |
| Additional member name grammar                                              | supported    | `MemberNames` (alphanumeric namespaces, may start with digit)|
| Reserved dedicated members in additional maps                               | supported    | Document, resource, identifier, relationship, error, jsonapi, link, source |
| Non-empty relationships                                                     | supported    | At least one of `data`, `links`, `meta`, or extension       |
| Links-only relationship minimum                                             | supported    | Non-pagination link locally; aggregate requires self/related/allowed ext/profile |
| Parameterized link media types                                              | supported    | RFC 7231 OWS (SP/HTAB) only immediately before `;`; leading/terminal bare-type and final-parameter trailing whitespace rejected; tokens; obs-text `%x80-FF`; HTAB qdtext; quoted-pair restricts CTLs |
| Link relation syntax                                                        | supported    | LOALPHA registered type or absolute URI; semantic keys use relation grammar |
| Absolute extension/profile URIs                                             | supported    | ASCII RFC 3986 absolute URI; structured authority (userinfo/host/port, IP-literal) |
| Unknown unnamespaced members rejected                                       | supported    | Aggregate validator; profile members allowed via context    |
| Extension namespace policy                                                  | supported    | `ValidationContext.allowedExtensionNamespaces`; includes link-object `meta` |
| Profile member policy                                                       | supported    | Additional members and policy-permitted link relations      |
| Profile URI policy                                                          | supported    | Enforced when `allowedProfileUris` is non-empty             |
| Duplicate resource identities                                               | supported    | `DUPLICATE_RESOURCE_IDENTITY`; structured identity; provisional lid aliases; per-array uniqueness for primary/relationship identifier collections |
| Full linkage                                                                | supported    | Canonical alias store/lookup; sparse-fieldset exception     |
| Consistent local identifiers                                                | supported    | Order-independent one-to-one `id`↔`lid` partners            |
| Context-specific link members                                               | supported    | `LinksContext`; `@` keys only via additional members        |
| Relationship pagination cardinality                                         | supported    | Derived from linkage; occurrence-keyed tri-state hints (`TO_ONE`/`TO_MANY`) for link-only |
| Top-level pagination requires collection                                    | supported    | Proven from `ResourceCollection` and `IdentifierCollection` |
| Open JSON value shapes                                                      | supported    | Immutable numbers; cycles rejected with stable diagnostics  |
| Null collection payloads and elements                                       | supported    | Stable `NULL_COLLECTION_*` codes; empty lists remain valid  |
| Null required single payloads                                               | supported    | `NULL_REQUIRED_VALUE` for single resource/identifier/linkage |
| Validation context null diagnostics                                         | supported    | `NULL_REQUIRED_VALUE` for usage, links context, policy sets/elements, hints |
| Resource type member-name grammar                                           | supported    | Only `null` is missing; Unicode-legal types accepted via `MemberNames` |
| Defensive collection copies                                                 | supported    | Model types and `ValidationContext`                         |
| URI-reference syntax                                                        | supported    | ASCII RFC 3986; structured authority; empty string allowed; raw non-ASCII rejected |

## Annotation metadata (Phase 1.2 — supported)

| Rule                                                         | Status    | Notes                                                                 |
|--------------------------------------------------------------|-----------|-----------------------------------------------------------------------|
| `@JsonApiResource(type)` on domain types                     | supported | Runtime-retained, `@Documented`; not `@Inherited`                     |
| `@JsonApiId` marker on logical properties                    | supported | Fields, methods, parameters, and record components                    |
| `@JsonApiAttribute(name)` optional attribute rename          | supported | Empty `name()` retains Jackson's logical property name                |
| `@JsonApiRelationship(name)` linkage metadata                | supported | Name only; no inclusion, fetch, cascade, or persistence elements      |

## Codec / wire format (Phase 2.1 — deferred)

| Rule                                             | Status   |
|--------------------------------------------------|----------|
| JSON serialization / deserialization             | deferred |
| Canonical member ordering                        | deferred |
| Golden fixture round-trips                       | deferred |
| Malformed input diagnostics with source location | deferred |

## Domain mapping (Phase 2.2 — deferred)

| Rule                             | Status   |
|----------------------------------|----------|
| Jackson-visible property mapping | deferred |

## Query parameters (Phase 3.1 — delegated)

| Rule                                                  | Status    |
|-------------------------------------------------------|-----------|
| `filter`, `sort`, `page`, `fields`, `include` parsing | delegated |

## HTTP / endpoints (out of scope)

| Rule                                          | Status       |
|-----------------------------------------------|--------------|
| Endpoint availability and operation semantics | out of scope |
| HTTP status selection                         | out of scope |
| Content negotiation beyond adapter            | out of scope |
