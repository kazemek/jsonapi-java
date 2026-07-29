---
name: milestone-review
description: Produces an evidence-based code review against a project milestone and writes the result to `.agentWork/.session/`. Use when the user explicitly requests a milestone review, phase audit, or acceptance review against `.agentWork/milestones/`.
disable-model-invocation: true
---

# Milestone Review

Review an implementation against one milestone contract. Do not fix findings or modify source, tests, milestones, vision, or ADRs unless the user separately asks for changes.

## Resolve the review inputs

1. Identify exactly one target file under `.agentWork/milestones/`.
   - Use a path, phase, or milestone name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple milestones are plausible, ask the user to choose.
2. Read:
   - the target milestone;
   - `docs/vision.md`;
   - `.agentWork/milestones/README.md`;
   - relevant records under `docs/adr/`;
   - `AGENTS.md`.
3. Determine the implementation change set in this order:
   - a diff, pull request, commit range, or paths supplied by the user;
   - branch changes and uncommitted changes when Git metadata is available;
   - source and test files implicated by the milestone when no diff is available.
4. State the reviewed change-set boundary in the artifact. Never imply that unexamined code was reviewed.

## Perform the review

1. Map the milestone goal, deliverables, non-goals, dependencies, and each acceptance criterion to implementation evidence.
2. Inspect relevant production code, tests, configuration, and documentation.
3. Look first for:
   - incorrect behavior and regressions;
   - unmet or contradicted milestone requirements;
   - vision or ADR conflicts;
   - missing validation, error handling, boundary cases, or tests;
   - accidental work outside the milestone scope;
   - nullness drift against ADR-009: missing `@NullMarked` on production packages, missing
     `@Nullable` on absence-null or null-preserving APIs, foreign nullness annotations
     (JetBrains/JSR-305/Checker), or treating explicit JSON `null` as a bare `@Nullable`
     instead of a sealed wire-null variant.
4. Run the narrowest relevant tests when practical, then run `./gradlew clean build` when the milestone or repository policy requires it.
   - Do not change implementation files to make tests pass.
   - Record commands, outcomes, and any inability to run them.
5. Treat milestone checkboxes as claims to verify, not as proof. Do not edit their checked state.
6. Assign each actionable finding a severity:
   - **Critical:** unsafe to ship or fundamentally invalidates the milestone.
   - **High:** user-visible defect, major contract violation, or likely regression.
   - **Medium:** real correctness, maintainability, or test-coverage gap with bounded impact.
   - **Low:** minor issue worth fixing; omit optional style preferences.
7. Give every finding:
   - a concise title;
   - file and line evidence;
   - the affected milestone requirement;
   - impact;
   - a concrete recommended correction.

## Choose the verdict

- **Pass:** no actionable findings remain and acceptance criteria have sufficient evidence.
- **Changes required:** one or more actionable findings remain.
- **Blocked:** the review cannot reach a reliable conclusion because required implementation, dependencies, or evidence are unavailable.

A failed build caused by the reviewed change is a finding. An unrelated or pre-existing failure is review evidence and may make the verdict `Blocked`; identify it as such rather than attributing it to the change without proof.

## Write the artifact

Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/milestone-review-<milestone-basename>.md
```

For `.agentWork/milestones/phase-1-1-spec-data-model.md`, write `.agentWork/.session/milestone-review-phase-1-1-spec-data-model.md`.

Use this template:

```markdown
# Milestone Review: <milestone title>

> **Milestone:** `<milestone path>`
> **Review scope:** <diff, commit range, or inspected paths>
> **Verdict:** Pass | Changes required | Blocked

## Summary

<Concise conclusion and the most important evidence.>

## Findings

### <Severity>: <finding title>

- **Location:** `<path>:<line or range>`
- **Milestone requirement:** <deliverable or acceptance criterion>
- **Impact:** <why this matters>
- **Recommendation:** <specific correction>

<Repeat in descending severity. Write "No actionable findings." when none exist.>

## Milestone coverage

- [Pass | Fail | Partial | Not verified] <goal, deliverable, non-goal, dependency, or acceptance criterion>
  - Evidence: <paths, lines, tests, or explanation>

## Verification

- `<command>` — Pass | Fail | Not run
  - Evidence: <result or reason>

## Residual risks

<Unverified behavior, unavailable evidence, dependency concerns, or "None identified.">
```

The artifact is ephemeral and non-canonical. On every re-review, replace the prior artifact for that milestone instead of appending history. After writing it, report the artifact path and verdict to the user.
