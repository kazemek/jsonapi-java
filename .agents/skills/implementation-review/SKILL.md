---
name: implementation-review
description: Produces an evidence-based code review against an implementation plan and writes the result to `.agentWork/.session/`. Use when the user explicitly requests an implementation review or acceptance review against `.agentWork/plans/`, or when `implement-plan` delegates review to a fresh-context subagent.
disable-model-invocation: true
---

# Implementation Review

Review an implementation against one plan contract. Do not fix findings or modify source, tests,
plans, vision, or ADRs unless the user separately asks for changes. Do not reconcile dependent live
plans; that is `implement-plan` finalization after Pass.

## Resolve inputs

1. Identify exactly one target file under `.agentWork/plans/`.
   - Use a path or plan name supplied by the user.
   - Infer the target only when the conversation or current work identifies one unambiguously.
   - If multiple plans are plausible, ask the user to choose.
   - Do not infer the next review target by scanning `.agentWork/plans/` as a backlog.
2. Read the plan, `AGENTS.md`, affected module READMEs and package documentation, and linked ADRs or
   conformance sections. Read `docs/vision.md` for direction, module, or public-boundary changes or
   a suspected conflict, and relevant Outlook only for unbuilt or revisable future direction.
   Expand only to records directly implicated by the change.
3. Determine the implementation change set in this order:
   - a diff, pull request, commit range, or paths supplied by the user;
   - branch changes and uncommitted changes when Git metadata is available;
   - source and test files implicated by the plan when no diff is available.
4. State the reviewed change-set boundary in the artifact. Never imply that unexamined code was
   reviewed.

External work-tracker access is never required to review a materialized plan. Tracker history is
coordination, not a substitute for missing repository knowledge. Do not search deleted plans for
current truth.

## Review

Map the goal, deliverables, non-goals, dependencies, and every acceptance criterion to production,
test, configuration, and documentation evidence. Follow Snapshot discovery before relevant Outlook.
Check:

1. **Correctness and scope:** incorrect behavior, regressions, unmet or contradicted requirements,
   Vision or ADR conflicts, missing validation/error handling/boundary cases/tests, and accidental
   work outside the plan.
2. **Nullness:** ADR-009 drift, including missing `@NullMarked` on production packages, missing
   `@Nullable` on absence-null or null-preserving APIs, foreign nullness annotations
   (JetBrains/JSR-305/Checker), or bare `@Nullable` in place of a sealed explicit wire-null variant.
3. **Module docs:** when public module surface changed, verify the canonical `module-docs`
   checklist and report stale or missing README sections, package docs, entry-point Javadoc, root
   registration, or duplicated Vision/ADR/conformance/root-workflow prose. Reference rather than
   copy its checks.
4. **Knowledge sync:** verify current Snapshot surfaces are updated or a no-change conclusion is
   justified; relevant Outlook is updated, reduced, or deleted when future assumptions changed and
   was not treated as current truth; no durable fact exists only in the disposable plan; and each
   newly durable fact has one canonical owner.
5. Run the narrowest relevant tests when practical, then run `./gradlew clean build` when the
   plan acceptance criteria or the change-scope gate tiers in `AGENTS.md` require it. For
   module production/test source changes (`jsonapi-java-*/src/**`), run or verify `spotless-format`
   before the build, then the build and `sonar-quality-gate`; `build` already runs `spotlessCheck`
   through `check`. Apply only the tier required for docs, workflow, or build configuration. Record
   every command, outcome, or inability to run it. Do not modify implementation to make tests pass.
6. Treat plan checkboxes as claims, not proof, and never edit them.
7. Do **not** fail the review merely because dependent live plans have not yet been rewritten.
   Dependency reconciliation is post-review finalization owned by `implement-plan` after Pass.

For every actionable finding, provide a concise title, file/line evidence, affected plan
requirement, impact, concrete correction, and severity:

- **Critical:** unsafe to ship or fundamentally invalidates the plan.
- **High:** user-visible defect, major contract violation, or likely regression.
- **Medium:** a real correctness, maintainability, or test-coverage gap with bounded impact.
- **Low:** a minor issue worth fixing; omit optional style preferences.

## Verdict

- **Pass:** no actionable findings remain and acceptance criteria have sufficient evidence,
  including Snapshot sync, Outlook sync, disposability, and canonical ownership.
- **Changes required:** one or more actionable findings remain.
- **Blocked:** the review cannot reach a reliable conclusion because required implementation,
  dependencies, or evidence are unavailable.

A failed build caused by the reviewed change is a finding. An unrelated or pre-existing failure is
review evidence and may make the verdict `Blocked`; identify it as such rather than attributing it
to the change without proof.

Unreconciled dependent live plans are not a Pass failure.

## Artifact

Create `.agentWork/.session/` if needed, then create or completely replace:

```text
.agentWork/.session/implementation-review-<plan-basename>.md
```

Use the template in [reference.md](reference.md).

The artifact is ephemeral and non-canonical. On every re-review, replace the prior artifact for that
plan instead of appending history. After writing it, report the artifact path and verdict to the
user.
