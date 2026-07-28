---
name: module-docs
description: Creates or updates dual-audience module documentation (README, package-info, entry-point Javadoc, root/AGENTS links). Use when adding a submodule, changing a module’s public packages or entry points, or when the user asks to document or refresh module docs.
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
3. Create or update `<module>/README.md` with this shape:
   - Title + one-line purpose
   - **Packages** table (package → role)
   - **Minimal usage** (short code sample when an entry point exists)
   - **Non-goals** (link ADR-007 / vision; do not restate the product boundary at length)
   - **Further reading** (conformance, relevant ADRs, root `AGENTS.md`)
   - **For contributors / agents** (module-specific bullets only: local vs aggregate rules, diagnostics, tests, extension policy, etc.)
4. Ensure `package-info.java` exists for each public package and for any documented internal package.
5. Ensure focused Javadoc on public entry points only (construction vs validation, wire-state distinctions)—not every type.
6. Ensure root `README.md` and `AGENTS.md` link to the module README when the module is present.
7. Do **not** duplicate `docs/vision.md`, full ADR bodies, or `docs/conformance.md` into the module README—link out.
8. Report paths created or updated.

## Checklist

- [ ] `<module>/README.md` covers purpose, packages, usage, non-goals, further reading, agents subsection
- [ ] `package-info.java` for each public (and documented internal) package
- [ ] Entry-point Javadoc on the module’s primary public types
- [ ] Root `README.md` lists the module
- [ ] `AGENTS.md` discovery/conventions point at `<module>/README.md`
- [ ] No duplicated vision/ADR/conformance prose

## Golden example

`jsonapi-java-core/README.md` plus `core.model` / `core.validation` / `core.internal` package-info is the reference. New modules should match that density and link-out style.
