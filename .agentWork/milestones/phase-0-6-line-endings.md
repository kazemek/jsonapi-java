# Phase 0.6 — Line Ending Enforcement

> **Scope:** Repository hygiene  
> **Dependencies:** Phase 0.5  
> **Status:** Complete

## Goal

Make LF the canonical line ending for text in the repository, while keeping Windows batch scripts on CRLF.

## Deliverables

- Add a root `.gitattributes` that sets `text=auto eol=lf` by default, `eol=crlf` for `*.bat` / `*.cmd`, and marks common binary extensions as `binary`.
- Add a root `.editorconfig` that sets UTF-8, LF, and final newlines for all files, with CRLF for `*.bat` / `*.cmd`.
- Renormalize tracked files so the index matches the attributes contract.
- Index this milestone in `.agentWork/milestones/README.md`.

## Non-goals

- Changing Spotless rules or formatter configuration.
- Adding indent or style EditorConfig beyond line endings, charset, and final newline.
- Changing developers' global `core.autocrlf` settings.

## Acceptance criteria

- [x] `.gitattributes` and `.editorconfig` exist at the repository root with the agreed rules.
- [x] `git ls-files --eol` shows text files as LF in the index.
- [x] `gradlew.bat` remains `i/crlf` in the index.
- [x] `./gradlew clean build` passes without `SONAR_TOKEN`.
- [x] `.agentWork/milestones/README.md` lists Phase 0.6 in dependency order and the milestone index.
