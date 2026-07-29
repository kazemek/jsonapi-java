---
name: module-docs
description: Creates or updates dual-audience module documentation (README, package-info, entry-point Javadoc, root module registration). Use when adding a submodule, changing a module’s public packages or entry points, or when the user asks to document or refresh module docs.
disable-model-invocation: true
---

# Module Docs

Keep thin dual-audience documentation for a Gradle submodule. Follow the pattern established by `jsonapi-java-core`. Do not invent a second style.

## When to run

Use this skill when:

- adding a new submodule under `settings.gradle.kts`;
- public packages, entry points, validate/read flows, non-goals, or agent-relevant invariants changed;
- the user asks to create or refresh module docs.

Skip when only internals or tests changed with no public-surface impact.

## Workflow

1. Resolve the target module directory (e.g. `jsonapi-java-core`).
2. Read existing `<module>/README.md` and `package-info.java` files; extend them in place.
3. Create or update `<module>/README.md` with these sections in order:
   - Title + one-line purpose
   - **Packages** table (package → role)
   - **Minimal usage** (always present: short code sample when an entry point exists; otherwise a
     concise note that no usable entry point exists yet)
   - **Non-goals** (link ADR-007 / vision; do not restate the product boundary at length)
   - **Further reading** (only relevant conformance, ADR, and root-agent links; titles or short
     labels must make their relevance clear)
   - **For contributors / agents** (module-specific invariants only: local vs aggregate rules,
     diagnostics, tests, extension policy, etc.; do not repeat root build, CI, or planning workflow)
4. Ensure `package-info.java` exists for every production package.
   Every production `package-info.java` must be `@NullMarked` (`org.jspecify.annotations`) and briefly
   state absence (`@Nullable`) versus wire-null (sealed types) when the package holds document model
   types (see ADR-009).
   Keep package documentation role-focused: describe its responsibility, public or internal
   boundary, and the contract needed before opening its types. Link to policy sources instead of
   copying them.
5. Ensure focused Javadoc on public entry points only (construction vs validation, wire-state distinctions)—not every type.
6. Ensure the root `README.md` lists and links the module. Keep `AGENTS.md` generic: it routes
   through `<module>/README.md` and must not accumulate one link per module.
7. Do **not** duplicate `docs/vision.md`, full ADR bodies, or `docs/conformance.md` into the module README—link out.
8. Re-read the golden example and compare section order, density, link-out behavior, agent notes,
   and package roles before reporting completion.
9. Report paths created or updated.

## Checklist

- [ ] `<module>/README.md` follows the golden section order, keeps each section compact, and always
      includes **Minimal usage** (code sample or explicit no-entry-point note)
- [ ] Agents subsection includes nullness bullets when the module has Java production packages (ADR-009)
- [ ] Agents subsection contains module-only invariants, not root build/CI/planning instructions
- [ ] `package-info.java` for every production package, each `@NullMarked` and role-focused
- [ ] Entry-point Javadoc on the module’s primary public types
- [ ] Root `README.md` lists the module
- [ ] `AGENTS.md` retains the generic `<module>/README.md` discovery route
- [ ] Vision, ADR, conformance, build, CI, and root workflow prose is linked rather than duplicated
- [ ] Final self-check confirms density and link-out behavior against the golden example

## Golden example

`jsonapi-java-core/README.md` plus `core.model` / `core.validation` / `core.internal` package-info is
the reference. New modules should match that density and link-out style: model packages document
wire/nullness distinctions, service packages document aggregate behavior and diagnostics, and
internal packages state that they are not public API.
