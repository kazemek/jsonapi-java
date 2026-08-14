---
name: implementation-review
description: Produces an evidence-based code review against an implementation plan and writes the result to `.agentWork/.session/`. Use when the user explicitly requests an implementation review or acceptance review against `.agentWork/plans/`, or when `implement-plan` delegates review to a fresh-context subagent.
disable-model-invocation: true
---

# Implementation Review

Review an implementation against one plan contract. Do not fix findings or modify source, tests,
plans, vision, or ADRs unless the user separately asks for changes. Do not reconcile dependent live
plans; that is `implement-plan` finalization after Pass.

## Resolve the review inputs

1. Identify exactly one target file under `.agentWork/plans/`.
   - Use a path or plan name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple plans are plausible, ask the user to choose.
   - Do not infer the next review target by scanning `.agentWork/plans/` as a backlog.
2. Read:
   - the target plan;
   - `AGENTS.md`;
   - affected module READMEs and package documentation;
   - `docs/vision.md` when the plan changes project direction, modules, or public product
     boundaries, or when evidence suggests a vision conflict;
   - relevant Outlook when the plan or change set concerns unbuilt or revisable future direction;
   - ADRs and conformance sections linked by the plan or affected module documentation, plus
     additional records only when directly implicated by the change.
3. Determine the implementation change set in this order:
   - a diff, pull request, commit range, or paths supplied by the user;
   - branch changes and uncommitted changes when Git metadata is available;
   - source and test files implicated by the plan when no diff is available.
4. State the reviewed change-set boundary in the artifact. Never imply that unexamined code was
   reviewed.

Linear is never required to review a materialized plan. Linear history is coordination, not a
substitute for missing repository knowledge. Do not search deleted plans for current truth.

## Perform the review

1. Map the plan goal, deliverables, non-goals, dependencies, and each acceptance criterion to
   implementation evidence.
2. Inspect relevant production code, tests, configuration, and documentation. Follow normal
   Snapshot discovery, then inspect relevant Outlook.
3. Look first for:
   - incorrect behavior and regressions;
   - unmet or contradicted plan requirements;
   - vision or ADR conflicts;
   - missing validation, error handling, boundary cases, or tests;
   - accidental work outside the plan scope;
   - nullness drift against ADR-009: missing `@NullMarked` on production packages, missing
     `@Nullable` on absence-null or null-preserving APIs, foreign nullness annotations
     (JetBrains/JSR-305/Checker), or treating explicit JSON `null` as a bare `@Nullable`
     instead of a sealed wire-null variant.
   - module-documentation drift when public module surface changed: verify the canonical
     `module-docs` checklist and report missing or stale README sections, package documentation,
     root module registration, or duplicated vision/ADR/conformance/root-workflow prose. Reference
     the checklist; do not copy it into the review instructions.
   - Snapshot sync: affected current-knowledge surfaces are updated, or “no documentation change
     required” is justified because current knowledge remains accurate.
   - Outlook sync: relevant Outlook is updated, reduced, or deleted when the implementation
     changed future assumptions; Outlook was not treated as current truth.
   - disposability: no durable current or future fact exists **only** in the plan.
   - canonical ownership: each newly durable fact has one canonical repository owner rather than
     competing duplicated prose.
4. Run the narrowest relevant tests when practical, then run `./gradlew clean build` when the
   plan acceptance criteria or the change-scope gate tiers in `AGENTS.md` require it. For
   production/test source changes, run (or verify recorded evidence of) the `spotless-format` skill
   before the build — `build` already executes `spotlessCheck` via `check` — then run the build and
   the `sonar-quality-gate` skill; docs-only, workflow-only, and build-configuration changes need
   only their tier's gates. Record each command and its outcome in the artifact.
   - Do not change implementation files to make tests pass.
   - Record commands, outcomes, and any inability to run them.
5. Treat plan checkboxes as claims to verify, not as proof. Do not edit their checked state.
6. Do **not** fail the review merely because dependent live plans have not yet been rewritten.
   Dependency reconciliation is post-review finalization owned by `implement-plan` after Pass.
7. Assign each actionable finding a severity:
   - **Critical:** unsafe to ship or fundamentally invalidates the plan.
   - **High:** user-visible defect, major contract violation, or likely regression.
   - **Medium:** real correctness, maintainability, or test-coverage gap with bounded impact.
   - **Low:** minor issue worth fixing; omit optional style preferences.
8. Give every finding:
   - a concise title;
   - file and line evidence;
   - the affected plan requirement;
   - impact;
   - a concrete recommended correction.

## Choose the verdict

- **Pass:** no actionable findings remain and acceptance criteria have sufficient evidence,
  including Snapshot sync, Outlook sync, disposability, and canonical ownership.
- **Changes required:** one or more actionable findings remain.
- **Blocked:** the review cannot reach a reliable conclusion because required implementation,
  dependencies, or evidence are unavailable.

A failed build caused by the reviewed change is a finding. An unrelated or pre-existing failure is
review evidence and may make the verdict `Blocked`; identify it as such rather than attributing it
to the change without proof.

Unreconciled dependent live plans are not a Pass failure.

## Write the artifact

Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/implementation-review-<plan-basename>.md
```

For `.agentWork/plans/jackson3-presence-aware-patch-binding.md`, write
`.agentWork/.session/implementation-review-jackson3-presence-aware-patch-binding.md`.

Use the template in [reference.md](reference.md).

The artifact is ephemeral and non-canonical. On every re-review, replace the prior artifact for that
plan instead of appending history. After writing it, report the artifact path and verdict to the
user.
