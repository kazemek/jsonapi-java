# ADR-001: Document Codec as the Product Boundary

**Status:** Accepted  
**Date:** 2026-07-26

## Context

JSON:API specifies both a document format and HTTP behavior. A library can reliably guarantee the documents it reads and writes, but it cannot guarantee application-owned endpoint, persistence, authorization, or query behavior without becoming an API framework.

The project aims to stay lightweight and non-intrusive.

## Decision

The primary product is a JSON:API v1.1 document model, validator, and codec.

Domain mapping, query parsing, and Spring integration are optional adapters. Applications own endpoint semantics, persistence, relationship mutation, authorization, query execution, supported profiles, and extension behavior.

Compliance is reported per feature as supported, pass-through, delegated, deferred, or out of scope. The project does not claim that an application is globally JSON:API compliant merely because it uses the library.

## Consequences

- Core behavior has an objective fixture- and rule-based conformance target.
- The project does not generate repositories, controllers, or resource operations.
- HTTP compliance claims are limited to behavior implemented by a web adapter.
- Extension and profile members can be preserved without the library implementing their semantics.
- Marketing language must use scoped, verifiable claims.
