---
name: module-docs
description: Creates or updates dual-audience module documentation (README, package-info, entry-point Javadoc, root module registration). Use when adding a submodule, changing a module’s public packages or entry points, or when the user asks to document or refresh module docs.
disable-model-invocation: true
---

# Module Docs

Keep thin dual-audience documentation for a Gradle submodule. Follow the `jsonapi-java-core`
golden example; do not invent a second style.

## When to run

Use this skill when:

- adding a new submodule under `settings.gradle.kts`;
- public packages, entry points, validate/read flows, non-goals, or agent-relevant invariants changed;
- the user asks to create or refresh module docs.

Skip when only internals or tests changed with no public-surface impact.

## Checklist

1. Resolve the target module directory (e.g. `jsonapi-java-core`).
2. Read existing `<module>/README.md` and `package-info.java` files; extend them in place.
3. Create or update `<module>/README.md` with these sections in order:
   - Title + one-line purpose
   - **Packages** table (package → role)
   - **Minimal usage** (always present: short code sample when an entry point exists; otherwise a
     concise note that no usable entry point exists yet)
   - **Non-goals** (link ADR-007 / vision; do not restate the product boundary at length)
   - **Further reading** (only relevant canonical links, such as Vision, conformance, ADR,
     build/CI, and root-agent sources; titles or short labels must make their relevance clear)
   - **For contributors / agents** (module-specific invariants only: local vs aggregate rules,
     diagnostics, tests, extension policy, etc.; include ADR-009 nullness bullets for Java
     production packages; do not repeat root build, CI, or planning workflow)
4. Ensure `package-info.java` exists for every production package.
   Every production `package-info.java` must be `@NullMarked` (`org.jspecify.annotations`) and briefly
   state absence (`@Nullable`) versus wire-null (sealed types) when the package holds document model
   types (see ADR-009).
   Keep package documentation role-focused: describe its responsibility, public or internal
   boundary, and the contract needed before opening its types. Link to policy sources instead of
   copying them.
5. Ensure focused Javadoc on public entry points only, including construction versus validation and
   wire-state distinctions where relevant; do not document every type.
6. Ensure the root `README.md` lists and links the module. Keep `AGENTS.md` generic: it routes
   through `<module>/README.md` and must not accumulate one link per module.
7. Link to relevant Vision, ADR, conformance, build, CI, and root-workflow sources rather than
   duplicating their prose. Prefer the root module registry for unbuilt surfaces; never invent
   current-capability prose for modules that do not exist yet.
8. Re-read the golden example and verify section order, compact density, minimal usage, package
   roles, nullness, entry-point Javadoc, root registration, link-out behavior, and agent notes.
9. Report every path created or updated.

## Golden example

`jsonapi-java-core/README.md` plus `core.model` / `core.validation` / `core.internal` package-info is
the reference. New modules should match that density and link-out style: model packages document
wire/nullness distinctions, service packages document aggregate behavior and diagnostics, and
internal packages state that they are not public API.
