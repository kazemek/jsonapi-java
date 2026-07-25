# ADR-003: Strict Construction and Aggregate Validation

**Status:** Accepted  
**Date:** 2026-07-26

## Context

Permissive record constructors plus optional validating factories create two incompatible validity contracts. Records containing caller-owned maps and lists can also change after validation.

Some JSON:API rules are local to one value, while included-resource identity and full linkage require a complete document.

## Decision

Enforce local invariants in the only public construction path. Jackson must not bypass validation through a raw constructor.

Run a separate aggregate validator for rules requiring document context. Validation failures carry a stable rule code and JSON Pointer-like path.

Defensively copy all collections. Open JSON maps and lists are copied recursively. Link maps use a null-preserving ordered copy rather than `Map.copyOf`.

Strict parsing is the default. A future lenient mode, if justified, must return explicit diagnostics and may not silently label an invalid document as valid.

## Consequences

- Programmatic and parsed documents share one validity contract.
- Documents cannot be invalidated by later collection mutation.
- Cross-document validation is explicit and independently testable.
- Construction is more deliberate than bare record instantiation.
- Validation context is required for configured extension and profile members.
