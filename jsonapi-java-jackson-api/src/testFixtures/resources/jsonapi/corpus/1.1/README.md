# JSON:API 1.1 document fixtures

Version-neutral JSON:API documents used by adapter tests. Jackson 3 and (later) Jackson 2 tests share
this corpus; do not fork major-specific copies. The files ship as classpath resources under
`jsonapi/corpus/1.1/` and are loaded with the small resource helper in the test-fixture source set.

## Layout

| Path                           | Role                                                                                          |
|--------------------------------|-----------------------------------------------------------------------------------------------|
| `manifest.json`                | Ordered inventory of valid fixture ids, JSON paths, and notes                                 |
| `ambiguous-manifest.json`      | Ordered inventory of shared dual-success ambiguous primary-data inputs                        |
| `negative-manifest.json`       | Ordered inventory of read-only negative inputs                                                |
| `documents/*.json`             | Pretty-printed canonical wire fixtures                                                        |
| `documents/*.compact.json`     | Compact canonical wire fixtures for member-order-sensitive inputs                             |
| `negative/*.json`              | Read-only inputs for the negative corpus (malformed or context-invalid documents)             |
| `envelope-binding/*.json`      | Named typed-envelope binding-variant documents (stable names; not codec corpus entries)       |
| `patch/*.json`                 | Named PATCH request documents; one resource serves both low-level and typed PATCH tests wherever the request wire form is identical |
| `domain-read/*.json`           | Named flat-read wire documents (currently the included-isolation pair)                        |

The manifest files are resource inventories only. Adapter tests select the files they need locally
and own diagnostics, locations, policies, decoded values, and other behavioral expectations in their
own specs. Documents under `envelope-binding/` and `patch/` are named directly by their resource
paths rather than through a shared runtime registry.

## Usage

Tests should make their input, action, and expected result visible in the adapter spec. Small local
tables or helpers are appropriate when several documents exercise the same operation and assertion
shape; shared code must remain limited to input data, application-shaped fixture types, and resource
loading.

## Adding a fixture

1. Add the JSON document under the appropriate directory.
2. Update a local adapter spec with the new resource path and expected behavior.
3. Add a compact sibling only when exact member order matters.
4. If the document intentionally fails the pinned draft schema, document that reason in the
   adapter spec.
5. Run the adapter tests and the normal repository completion gates.

## Negative corpus

`negative-manifest.json` records only the ids, paths, and notes for read-only inputs that must fail.
Each adapter owns the expected failure category, JSON pointer, validation rule code, source-location
expectations, and other diagnostics. Keep the inventory and its referenced files synchronized when
changing the negative corpus.

## Ambiguous primary data

`ambiguous-manifest.json` lists valid dual-success inputs whose decoded model depends on the explicit
primary-data kind. Each adapter proves both readings locally with the expected models.
