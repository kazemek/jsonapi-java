# JSON:API v1.1 Conformance Checklist

Conformance is reported per feature as: **supported**, **pass-through**, **delegated**, **deferred**, or **out of scope**.

This checklist is seeded by Phase 1.1 (`jsonapi-java-core`). Wire-format round-trips are **deferred** to Phase 2.1.

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
| Resource `id` required except create context                                | supported    | Aggregate `JsonApiDocumentValidator`                        |
| Local identifier (`lid`) for new resources                                  | supported    | `ResourceIdentifier`, aggregate validation                  |
| Attributes / relationships flat wrappers                                    | supported    | `Attributes`, `Relationships`                               |
| Absent vs present-empty attributes/relationships                            | supported    | `null` wrapper vs `empty()`                                 |
| `@` and extension members in containers                                     | pass-through | Preserved in `additionalMembers`; semantics not interpreted |
| Relationship linkage: absent, null, single, collection                      | supported    | `RelationshipData` sealed variants                          |
| Link-only and meta-only relationships                                       | supported    | `Relationship`                                              |
| Links: string and object forms                                              | supported    | Sealed `Link`                                               |
| `hreflang` canonical list representation                                    | supported    | `Link.ObjectLink`; codec emission deferred                  |
| Nullable pagination links                                                   | supported    | `Links` null-preserving map                                 |
| Meta flat object (no synthetic `members` key)                               | supported    | `Meta`                                                      |
| Error object requires ≥1 standard member                                    | supported    | `ErrorObject`                                               |
| Additional member name grammar                                              | supported    | `MemberNames` (v1.1 charset; alphanumeric namespaces)       |
| Reserved dedicated members in additional maps                               | supported    | Document, resource, identifier, error, jsonapi              |
| Non-empty relationships                                                     | supported    | At least one of `data`, `links`, `meta`, or extension       |
| Parameterized link media types                                              | supported    | `SyntaxValidators.isValidMediaType`                         |
| Unknown unnamespaced members rejected                                       | supported    | Aggregate validator; profile members allowed via context    |
| Extension namespace policy                                                  | supported    | `ValidationContext.allowedExtensionNamespaces`              |
| Profile member policy                                                       | supported    | `ValidationContext.allowedProfileMemberNames`               |
| Profile URI policy                                                          | supported    | Enforced when `allowedProfileUris` is non-empty             |
| Duplicate included resources                                                | supported    | Aggregate validator                                         |
| Full linkage                                                                | supported    | Transitive via included relationships; sparse-fieldset exception |
| Consistent local identifiers                                                | supported    | Aggregate validator                                         |
| Context-specific link members                                               | supported    | `LinksContext`                                              |
| Relationship pagination cardinality hint                                    | supported    | `relationshipPaginationHints`                               |
| Open JSON value shapes                                                      | supported    | `OpenJsonValues`                                            |
| Defensive collection copies                                                 | supported    | All model types                                             |

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
