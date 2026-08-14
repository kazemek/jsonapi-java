# Phase 3.1 — Optional Query-Parameter Parser

> **Module:** `jsonapi-java-query`  
> **Package:** `io.github.kazemek.jsonapi.query`  
> **Dependencies:** None beyond `java.base`  
> **Status:** Not started
> **Work item:** KAZ-26

## Goal

Parse JSON:API query parameters into immutable, framework-neutral values without executing filtering, sorting, pagination, sparse fieldsets, or inclusion.

The module is optional because query parameters are an HTTP-facing feature, not part of the document model.

## Public result types

The initial model contains seven records and one enum:

1. `IncludeParam`
2. `RelationshipPath`
3. `SparseFieldsets`
4. `SortParam`
5. `SortField`
6. `SortDirection`
7. `PageParam`
8. `FilterParam`

Type count is descriptive, not an architectural constraint. Parser or diagnostic types may be added when they improve the contract.

## Parsing contracts

### Include

- A comma separates relationship paths.
- A period separates path segments.
- An empty value means no related resources are requested.
- Empty segments, empty entries, and illegal relationship member names are rejected.
- Duplicate paths are removed while preserving first occurrence order.

### Sparse fieldsets

- `fields[TYPE]` values are comma-separated field names.
- An empty value means no fields for that type.
- Resource types and field names use the JSON:API member-name grammar.
- Repeated declarations for the same type are rejected rather than merged implicitly.
- Field order and first occurrence are retained; duplicate fields collapse.

### Sorting

- Commas separate criteria and order is retained.
- A leading `-` means descending; otherwise the criterion is ascending.
- A bare `-`, empty criterion, or illegal path is rejected.
- Dot-separated sort paths are supported.

### Page and filter families

JSON:API reserves the families but does not define execution semantics.

- `PageParam` and `FilterParam` retain normalized full family keys and ordered lists of values.
- Nested brackets, bracketed relationship paths, empty brackets, and repeated full parameter names remain representable.
- No numeric coercion, operator interpretation, filter DSL, or pagination strategy is built in.

## Input boundary

The parser consumes ordered decoded name/value pairs after `application/x-www-form-urlencoded` processing. It does not read an `HttpServletRequest`.

The adapter contract must preserve:

- repeated names and their value order;
- encoded and unencoded square brackets as equivalent names;
- empty values;
- the distinction between an absent parameter and a present empty value.

Tests document delimiter and percent-decoding order. Framework adapters must not collapse the input into `Map<String, String>` before parsing.

## Error contract

Malformed syntax produces a query diagnostic with:

- a stable rule code;
- the complete parameter name;
- the offending value where safe;
- an offset or path when available.

The query module does not choose an HTTP status. The Spring adapter maps unsupported or malformed supported parameters to JSON:API `400 Bad Request` error documents.

## Test strategy

Use data-driven Spock specifications for:

- single, multiple, nested, empty, and duplicate include paths;
- sparse fieldsets for multiple types and repeated declarations;
- ascending, descending, multi-field, and relationship-path sorts;
- page and filter families with nested brackets, empty brackets, and repeated values;
- encoded and unencoded brackets;
- absent versus present-empty parameters;
- every malformed comma, period, bracket, and minus edge case;
- stable diagnostics;
- defensive copying and order preservation.

## Acceptance criteria

- [ ] All eight result types are immutable and preserve defined ordering.
- [ ] No API reduces repeated query parameters to a single string.
- [ ] Include, sparse-fieldset, and sort grammar follows JSON:API v1.1.
- [ ] Page and filter values remain strategy-neutral.
- [ ] Parsing has no servlet, Spring, Jackson, or core-document dependency.
- [ ] Malformed inputs return stable structured diagnostics.
- [ ] `./gradlew :jsonapi-java-query:test` passes.
