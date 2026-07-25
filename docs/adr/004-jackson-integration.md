# ADR-004: Jackson Introspection Is Authoritative

**Status:** Accepted  
**Date:** 2026-07-26

## Context

An independent field/component/getter scanner would disagree with Jackson about logical properties, visibility, mix-ins, naming, ignored values, creators, and custom serializers. That would make the mapping surprising and invalidate the claim that it behaves like normal Jackson serialization.

Document envelopes such as links and metadata also do not have the default record wire shape.

## Decision

Use Jackson's introspection and logical property model for domain mapping. Do not establish independent field-first or getter-first discovery.

Implement explicit codecs for JSON:API document structures, including:

- flat links and metadata;
- absent versus explicit-null data;
- sealed primary and relationship linkage;
- string and object links;
- additional members;
- strict validation during reads.

Jackson-visible properties become attributes by default after identifier and relationship roles are applied. Jackson ignores, names, mix-ins, serializers, and creator metadata remain authoritative unless a documented JSON:API annotation overrides the JSON:API role or field name.

## Consequences

- Domain mapping follows familiar Jackson behavior.
- Record annotation propagation is resolved as one logical property.
- The Jackson module is more than a default record serializer.
- Exact wire fixtures are required before the core API is considered stable.
- Supporting a Jackson feature means proving it with an integration test, not assuming reflection preserves it.
